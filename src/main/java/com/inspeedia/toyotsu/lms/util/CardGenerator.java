package com.inspeedia.toyotsu.lms.util;

import com.inspeedia.toyotsu.lms.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

@Service
public class CardGenerator {
    Logger log = LoggerFactory.getLogger(CardGenerator.class);

    Rectangle boxA = new Rectangle(2, 2, 254, 249);
    Rectangle profileImageArea = new Rectangle(5, 5, 249, 244);
    Rectangle badgeImageArea = new Rectangle(12, 12, 40, 40);
    Rectangle boxB = new Rectangle(2, 251, 254, 106);
    Rectangle companyNameArea = new Rectangle(5, 254, 249, 101);
    Rectangle boxC = new Rectangle(256, 2, 381, 177);
    Rectangle employeeNameArea = new Rectangle(259, 5, 376, 172);
    Rectangle boxD = new Rectangle(256, 179, 381, 178);
    Rectangle processNameArea = new Rectangle(259, 182, 376, 173);
    Font primary = new Font("Arial", Font.PLAIN, 24);
    Font primaryBold = new Font("Arial", Font.BOLD, 24);
    Font secondary = new Font("Arial", Font.PLAIN, 16);
    Font secondaryBold = new Font("Arial", Font.BOLD, 16);
    public byte[] generate(int width, int height, InputStream profileImageStream, InputStream badgeImageStream, Employee data) {
        byte[] cardImageData = null;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String employeeName = data.getName();
        String companyName = data.getCompanyName();
        String mainProcess = data.getMainProcess();
        String subProcess = data.getSubProcess();
        try {
            int lineThickness = 5;
            // Set background color and clear the canvas
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // Layout
            g.setColor(Color.YELLOW);
            g.fillRect(employeeNameArea.x, employeeNameArea.y, employeeNameArea.width, employeeNameArea.height);
            g.setColor(Color.WHITE);
            g.fillRect(companyNameArea.x, companyNameArea.y, companyNameArea.width, companyNameArea.height);
            g.setColor(Color.GREEN);
            g.fillRect(processNameArea.x, processNameArea.y, processNameArea.width, processNameArea.height);
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setStroke(new BasicStroke(lineThickness));
            g.setColor(Color.BLACK);
            g.drawRect(boxA.x, boxA.y, boxA.width, boxA.height);
            g.drawRect(boxB.x, boxB.y, boxB.width, boxB.height);
            g.drawRect(boxC.x, boxC.y, boxC.width, boxC.height);
            g.drawRect(boxD.x, boxD.y, boxD.width, boxD.height);

            g.setColor(Color.BLACK);
            drawCenteredText(g, primaryBold, employeeName, employeeNameArea);
            drawCenteredText(g, primaryBold, companyName, companyNameArea);
            drawWrappedText(g, subProcess, primary, 20, processNameArea);
            BufferedImage profileImage = ImageIO.read(profileImageStream);
            g.drawImage(profileImage, profileImageArea.x,profileImageArea.y, profileImageArea.width
                    , profileImageArea.height, null);

            BufferedImage badgeImage = ImageIO.read(badgeImageStream);
            g.drawImage(badgeImage, badgeImageArea.x, badgeImageArea.y, badgeImageArea.width
                    , badgeImageArea.height, null);
            try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", outputStream);

                cardImageData = outputStream.toByteArray();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            log.error(ex.getMessage(), ex);
        }
        finally {
            g.dispose();
        }

        return cardImageData;
    }


    public double getCenteredTextPointX(Rectangle2D srcArea, Rectangle2D textBound) {
        return (srcArea.getX() + (srcArea.getWidth() / 2)) - textBound.getCenterX();
    }

    public double getCenteredTextPointY(Rectangle2D srcArea, Rectangle2D textBound) {
        return (srcArea.getY() + (srcArea.getHeight() / 2)) - textBound.getCenterY();
    }

    public void drawCenteredText(Graphics2D g, Font font, String string, Rectangle2D drawArea) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D bound = fm.getStringBounds(string, g);
        g.drawString(string, (float)getCenteredTextPointX(drawArea, bound)
                , (float)getCenteredTextPointY(drawArea, bound));
    }

    private void drawWrappedText(Graphics2D graphics, String text, Font font, double padding, Rectangle drawArea) {
        // Create an AttributedString with the given text and font
        AttributedString attributedText = new AttributedString(text);
        attributedText.addAttribute(java.awt.font.TextAttribute.FONT, font);
        AttributedCharacterIterator iterator = attributedText.getIterator();

        // Create a LineBreakMeasurer to handle line wrapping
        FontRenderContext frc = graphics.getFontRenderContext();
        LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
        drawArea = shrink(drawArea, drawArea, padding);

        float x = drawArea.x;
        float y = drawArea.y;

        // Loop through the text and draw each line
        while (measurer.getPosition() < iterator.getEndIndex()) {
            // Measure the next line that fits within the draw area's width
            TextLayout layout = measurer.nextLayout(drawArea.width);

            // Check if the next line exceeds the draw area's height
            if (y + layout.getAscent() > drawArea.y + drawArea.height) {
                break;
            }

            // Draw the text layout
            y += layout.getAscent();
            layout.draw(graphics, x, y);

            // Adjust y for the next line
            y += layout.getDescent() + layout.getLeading();
        }
    }

    public Rectangle shrink(Rectangle2D rect, Rectangle2D drawArea, double shrinkSize) {
        double x = (rect.getCenterX() - drawArea.getCenterX()) + shrinkSize + rect.getX();
        double y = (rect.getCenterY() - drawArea.getCenterY()) + shrinkSize + rect.getY();
        double w = rect.getWidth() - shrinkSize;
        double h = rect.getHeight() - shrinkSize;
        return new Rectangle((int)x, (int)y, (int)w, (int)h);
    }
}
