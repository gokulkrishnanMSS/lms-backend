package com.inspeedia.toyotsu.lms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inspeedia.toyotsu.lms.dto.EmployeeDTO;
import com.inspeedia.toyotsu.lms.dto.EmployeeWrapper;
import com.inspeedia.toyotsu.lms.enums.Skills;
import com.inspeedia.toyotsu.lms.model.Employee;
import com.inspeedia.toyotsu.lms.model.SkillType;
import com.inspeedia.toyotsu.lms.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


@Service
public class EmployeeService {
    private final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;
    private final EmployeeWrapper employeeWrapper;
    private final MinioService minioService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeWrapper employeeWrapper
            , MinioService minioService) {
        this.employeeRepository = employeeRepository;
        this.employeeWrapper = employeeWrapper;
        this.minioService = minioService;
    }

    public Employee addOperator(EmployeeDTO employeeDto, MultipartFile image) {
        String imageId = minioService.saveFile(image);
        employeeDto.setPhoto(imageId);
        Employee employee = employeeWrapper.toEntity(employeeDto);
        return employeeRepository.save(employee);
    }

    public void addNewEmployeeIntoExistingData(String department, Employee employee, LocalDate date) {
        try {
            // Retrieve JSON from MinIO
            String objectKey = "save-state-" + department + "-" + date + ".json";
            InputStream stream = minioService.getFile(objectKey);

            if (stream == null) {
                System.err.println("File not found in MinIO: " + objectKey);
                return;
            }

            // Parse JSON from MinIO
            JsonNode rawNode = objectMapper.readTree(stream);

            JsonNode rootNode;
            if (rawNode.isTextual()) {
                rootNode = objectMapper.readTree(rawNode.asText());
            } else {
                rootNode = rawNode;
            }

            // Retrieve listOfStates and ensure it is parsed correctly
            JsonNode listOfStatesNode = rootNode.get("listOfStates");

            if (listOfStatesNode != null && listOfStatesNode.isTextual()) {
                listOfStatesNode = objectMapper.readTree(listOfStatesNode.asText());
            }

            if (listOfStatesNode != null && listOfStatesNode.has("rosterList")) {
                ArrayNode rosterList = (ArrayNode) listOfStatesNode.get("rosterList");
                // Create a new employee JSON object
                ObjectNode newEmployee = objectMapper.createObjectNode();
                newEmployee.put("id", String.valueOf(employee.getId()));
                newEmployee.put("cardId", "item-" + employee.getId());
                newEmployee.put("fullName", employee.getName());
                newEmployee.put("department", department);
                newEmployee.put("companyName", employee.getCompanyName());
                newEmployee.put("photograph", employee.getPhoto() != null ? employee.getPhoto() : "");
                newEmployee.put("mainProcess", employee.getMainProcess());
                newEmployee.put("subOperation", employee.getSubProcess());
                newEmployee.put("assigned", false);
                newEmployee.put("employmentPeriod", employee.getEmploymentPeriod());
                newEmployee.put("qualityGrade", "");
                newEmployee.put("qualityResult", "Cases: " + employee.getQualityCases());
                newEmployee.put("productivityGrade", "");
                newEmployee.put("productivityPercentage", employee.getProductivity() + "%");
                newEmployee.put("suitableArea", employee.getSuitableArea());
                newEmployee.put("group", String.valueOf(employee.getGroupId()));
                newEmployee.put("impNotes", "");
                newEmployee.put("role", "WORKER");

                // Create and populate the skillGraph
                SkillType skillGraph = new SkillType("Employee Skill");
                skillGraph.setSkillLevel(Skills.A, employee.getSkillA());
                skillGraph.setSkillLevel(Skills.B, employee.getSkillB());
                skillGraph.setSkillLevel(Skills.C, employee.getSkillC());
                skillGraph.setSkillLevel(Skills.D, employee.getSkillD());
                skillGraph.setSkillLevel(Skills.E, employee.getSkillE());
                skillGraph.setSkillLevel(Skills.F, employee.getSkillF());
                newEmployee.set("skillGraph", objectMapper.valueToTree(skillGraph));

                // Append the new employee to the rosterList
                rosterList.add(newEmployee);

                // Convert back to JSON
                ((ObjectNode) rootNode).set("listOfStates", listOfStatesNode);

                String updatedJson = objectMapper.writeValueAsString(rootNode);

                // Save back to MinIO
                minioService.updateFile(objectKey, updatedJson);

                System.out.println("New employee added successfully!");
            } else {
                System.err.println("Error: 'rosterList' not found inside 'listOfStates'");
            }

        } catch (Exception e) {
            log.error("Error while adding new employee: {}", e.getMessage());
        }
    }

    public void removeEmployeeFromExistingData(String department, String employeeId, LocalDate date) {
        try {
            // Retrieve JSON from MinIO
            String objectKey = "save-state-" + department + "-" + date + ".json";
            InputStream stream = minioService.getFile(objectKey);

            if (stream == null) {
                System.err.println("File not found in MinIO: " + objectKey);
                return;
            }

            // Parse JSON from MinIO
            JsonNode rawNode = objectMapper.readTree(stream);
            JsonNode rootNode = rawNode.isTextual() ? objectMapper.readTree(rawNode.asText()) : rawNode;
            JsonNode listOfStatesNode = rootNode.get("listOfStates");

            if (listOfStatesNode != null && listOfStatesNode.isTextual()) {
                listOfStatesNode = objectMapper.readTree(listOfStatesNode.asText());
            }

            if (listOfStatesNode != null) {
                List<String> listNames = Arrays.asList(
                        "rosterList", "selectedRosterList", "absentList", "othersList",
                        "administratorList", "breakList", "tacList"
                );

                ObjectNode updatedListOfStates = (ObjectNode) listOfStatesNode;

                for (String listName : listNames) {
                    JsonNode listNode = listOfStatesNode.get(listName);
                    if (listNode != null && listNode.isArray()) {
                        ArrayNode updatedList = objectMapper.createArrayNode();
                        for (JsonNode node : listNode) {
                            if (!node.get("id").asText().equals(employeeId)) {
                                updatedList.add(node);
                            }
                        }
                        updatedListOfStates.set(listName, updatedList);
                    }
                }

                // Update productionLines
                JsonNode productionLinesNode = listOfStatesNode.get("productionLines");
                if (productionLinesNode != null && productionLinesNode.isArray()) {
                    for (JsonNode productionLine : productionLinesNode) {
                        ArrayNode suppliers = (ArrayNode) productionLine.get("suppliers");
                        if (suppliers != null) {
                            for (JsonNode supplier : suppliers) {
                                if (supplier.has("rosterItem") && supplier.get("rosterItem").has("id") &&
                                        supplier.get("rosterItem").get("id").asText().equals(employeeId)) {
                                    ((ObjectNode) supplier).putNull("rosterItem");
                                }

                                ArrayNode packers = (ArrayNode) supplier.get("packers");
                                if (packers != null) {
                                    for (JsonNode packer : packers) {
                                        if (packer.has("rosterItem") && packer.get("rosterItem").has("id") &&
                                                packer.get("rosterItem").get("id").asText().equals(employeeId)) {
                                            ((ObjectNode) packer).putNull("rosterItem");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Update armsExteriorList and procastExteriorList
                List<String> exteriorLists = Arrays.asList("armsExteriorList", "procastExteriorList");
                for (String exteriorList : exteriorLists) {
                    JsonNode exteriorListNode = listOfStatesNode.get(exteriorList);
                    if (exteriorListNode != null && exteriorListNode.isArray()) {
                        for (JsonNode exteriorItem : exteriorListNode) {
                            if (exteriorItem.has("rosterItem") && exteriorItem.get("rosterItem").has("id") &&
                                    exteriorItem.get("rosterItem").get("id").asText().equals(employeeId)) {
                                ((ObjectNode) exteriorItem).putNull("rosterItem");
                            }
                        }
                    }
                }

                // Save updated JSON back to MinIO
                ((ObjectNode) rootNode).set("listOfStates", updatedListOfStates);
                String updatedJson = objectMapper.writeValueAsString(rootNode);
                minioService.updateFile(objectKey, updatedJson);

                System.out.println("Employee removed successfully!");
            } else {
                System.err.println("Error: 'listOfStates' not found in JSON");
            }
        } catch (Exception e) {
            log.error("Error while removing employee: {}", e.getMessage());
        }
    }

    public void updateEmployeeInExistingData(Employee employee, LocalDate date) {
        try {
            // Retrieve JSON from MinIO
            String objectKey = "save-state-" + employee.getDepartment() + "-" + date + ".json";
            InputStream stream = minioService.getFile(objectKey);

            if (stream == null) {
                System.err.println("File not found in MinIO: " + objectKey);
                return;
            }

            // Parse JSON from MinIO
            JsonNode rawNode = objectMapper.readTree(stream);
            JsonNode rootNode = rawNode.isTextual() ? objectMapper.readTree(rawNode.asText()) : rawNode;
            JsonNode listOfStatesNode = rootNode.get("listOfStates");

            if (listOfStatesNode != null && listOfStatesNode.isTextual()) {
                listOfStatesNode = objectMapper.readTree(listOfStatesNode.asText());
            }

            if (listOfStatesNode != null) {
                List<String> listNames = Arrays.asList(
                        "rosterList", "selectedRosterList", "absentList", "othersList",
                        "administratorList", "breakList", "tacList"
                );

                ObjectNode updatedListOfStates = (ObjectNode) listOfStatesNode;

                for (String listName : listNames) {
                    JsonNode listNode = listOfStatesNode.get(listName);
                    if (listNode != null && listNode.isArray()) {
                        ArrayNode updatedList = objectMapper.createArrayNode();
                        for (JsonNode node : listNode) {
                            if (node.get("id").asText().equals(String.valueOf(employee.getId()))) {
                                updateRosterItem((ObjectNode) node, employee);
                            }
                            updatedList.add(node);
                        }
                        updatedListOfStates.set(listName, updatedList);
                    }
                }

                // Update productionLines
                JsonNode productionLinesNode = listOfStatesNode.get("productionLines");
                if (productionLinesNode != null && productionLinesNode.isArray()) {
                    for (JsonNode productionLine : productionLinesNode) {
                        ArrayNode suppliers = (ArrayNode) productionLine.get("suppliers");
                        if (suppliers != null) {
                            for (JsonNode supplier : suppliers) {
                                if (supplier.has("rosterItem") && supplier.get("rosterItem").has("id") &&
                                        supplier.get("rosterItem").get("id").asText().equals(String.valueOf(employee.getId()))) {
                                    updateRosterItem((ObjectNode) supplier.get("rosterItem"), employee);
                                }

                                ArrayNode packers = (ArrayNode) supplier.get("packers");
                                if (packers != null) {
                                    for (JsonNode packer : packers) {
                                        if (packer.has("rosterItem") && packer.get("rosterItem").has("id") &&
                                                packer.get("rosterItem").get("id").asText().equals(String.valueOf(employee.getId()))) {
                                            updateRosterItem((ObjectNode) packer.get("rosterItem"), employee);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Update armsExteriorList and procastExteriorList
                List<String> exteriorLists = Arrays.asList("armsExteriorList", "procastExteriorList");
                for (String exteriorList : exteriorLists) {
                    JsonNode exteriorListNode = listOfStatesNode.get(exteriorList);
                    if (exteriorListNode != null && exteriorListNode.isArray()) {
                        for (JsonNode exteriorItem : exteriorListNode) {
                            if (exteriorItem.has("rosterItem") && exteriorItem.get("rosterItem").has("id") &&
                                    exteriorItem.get("rosterItem").get("id").asText().equals(String.valueOf(employee.getId()))) {
                                updateRosterItem((ObjectNode) exteriorItem.get("rosterItem"), employee);
                            }
                        }
                    }
                }

                // Save updated JSON back to MinIO
                ((ObjectNode) rootNode).set("listOfStates", updatedListOfStates);
                String updatedJson = objectMapper.writeValueAsString(rootNode);
                minioService.updateFile(objectKey, updatedJson);

                System.out.println("Employee updated successfully!");
            } else {
                System.err.println("Error: 'listOfStates' not found in JSON");
            }
        } catch (Exception e) {
            log.error("Error while updating employee: {}", e.getMessage());
        }
    }

    private void updateRosterItem(ObjectNode node, Employee employee) {
        node.put("fullName", employee.getName());
        node.put("department", employee.getDepartment());
        node.put("companyName", employee.getCompanyName());
        node.put("photograph", employee.getPhoto() != null ? employee.getPhoto() : "");
        node.put("mainProcess", employee.getMainProcess());
        node.put("subOperation", employee.getSubProcess());
        node.put("employmentPeriod", employee.getEmploymentPeriod());
        node.put("qualityGrade", "");
        node.put("qualityResult", "Cases: " + employee.getQualityCases());
        node.put("productivityGrade", "");
        node.put("productivityPercentage", employee.getProductivity() + "%");
        node.put("suitableArea", employee.getSuitableArea());
        node.put("group", String.valueOf(employee.getGroupId()));
        node.put("skillGraph", "");
        node.put("impNotes", "");
        node.put("role", "WORKER");
        SkillType skillGraph = new SkillType("Employee Skill");
        skillGraph.setSkillLevel(Skills.A, employee.getSkillA());
        skillGraph.setSkillLevel(Skills.B, employee.getSkillB());
        skillGraph.setSkillLevel(Skills.C, employee.getSkillC());
        skillGraph.setSkillLevel(Skills.D, employee.getSkillD());
        skillGraph.setSkillLevel(Skills.E, employee.getSkillE());
        skillGraph.setSkillLevel(Skills.F, employee.getSkillF());
        node.set("skillGraph", objectMapper.valueToTree(skillGraph));
    }
}
