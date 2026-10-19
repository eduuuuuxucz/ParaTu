import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** An animated Cinnamoroll letter. Personalize carta.txt, then run this file. */
public class PatriciaBouquet {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Una carta especial");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new LetterPanel());
            window.setSize(1000, 720);
            window.setMinimumSize(new Dimension(760, 580));
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    static class LetterPanel extends JPanel {
        private static final Color INK = new Color(120, 79, 108);
        private static final Color PINK = new Color(248, 176, 203);
        private final Timer timer;
        private final String message = loadMessage();
        private final BufferedImage hearts = loadImage("cinnamoroll-hearts.png");
        private final BufferedImage teddy = loadImage("cinnamoroll-teddy.png");
        private final BufferedImage cookie = loadImage("cinnamoroll-cookie.png");
        private float progress;
        private boolean opening;

        LetterPanel() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            timer = new Timer(16, event -> {
                if (opening && progress < 1f) {
                    progress = Math.min(1f, progress + .012f);
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent event) { begin(); }
            });
            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_ENTER) begin();
                }
            });
            setFocusable(true);
        }

        private void begin() {
            requestFocusInWindow();
            if (!opening) {
                opening = true;
                timer.start();
            }
        }

        private String loadMessage() {
            try {
                return Files.readString(Path.of("carta.txt"), StandardCharsets.UTF_8).trim();
            } catch (IOException error) {
                return "Escribe aqui tu carta especial.\n\nCon mucho carino.";
            }
        }

        private BufferedImage loadImage(String name) {
            try {
                BufferedImage image = ImageIO.read(Path.of(name).toFile());
                return image == null ? null : removeEdgeBackground(image);
            }
            catch (IOException error) { return null; }
        }

        // The downloaded sticker files use a pale checkerboard instead of a true alpha channel.
        // Only the light pixels connected to an edge are removed, so Cinnamoroll stays intact.
        private BufferedImage removeEdgeBackground(BufferedImage source) {
            int width = source.getWidth(), height = source.getHeight();
            boolean[] background = new boolean[width * height];
            int[] queue = new int[width * height];
            int head = 0, tail = 0;
            for (int x = 0; x < width; x++) {
                tail = enqueueBackground(source, background, queue, tail, x, 0);
                tail = enqueueBackground(source, background, queue, tail, x, height - 1);
            }
            for (int y = 1; y < height - 1; y++) {
                tail = enqueueBackground(source, background, queue, tail, 0, y);
                tail = enqueueBackground(source, background, queue, tail, width - 1, y);
            }
            while (head < tail) {
                int point = queue[head++], x = point % width, y = point / width;
                if (x > 0) tail = enqueueBackground(source, background, queue, tail, x - 1, y);
                if (x + 1 < width) tail = enqueueBackground(source, background, queue, tail, x + 1, y);
                if (y > 0) tail = enqueueBackground(source, background, queue, tail, x, y - 1);
                if (y + 1 < height) tail = enqueueBackground(source, background, queue, tail, x, y + 1);
            }
            BufferedImage cleaned = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                int pixel = source.getRGB(x, y);
                cleaned.setRGB(x, y, background[y * width + x] ? pixel & 0x00FFFFFF : pixel);
            }
            return cleaned;
        }

        private int enqueueBackground(BufferedImage image, boolean[] background, int[] queue, int tail, int x, int y) {
            int index = y * image.getWidth() + x;
            if (background[index] || !isLightNeutral(image.getRGB(x, y))) return tail;
            background[index] = true;
            queue[tail++] = index;
            return tail;
        }

        private boolean isLightNeutral(int pixel) {
            int red = (pixel >>> 16) & 255, green = (pixel >>> 8) & 255, blue = pixel & 255;
            return red > 215 && green > 215 && blue > 215 && Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)) < 32;
        }

        @Override protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth(), height = getHeight();
            paintBackground(g, width, height);
            paintLetterScene(g, width, height);
            g.dispose();
        }

        private void paintBackground(Graphics2D g, int w, int h) {
            g.setPaint(new RadialGradientPaint(w * .5f, h * .28f, Math.max(w, h) * .78f,
                    new float[] {0f, .55f, 1f},
                    new Color[] {new Color(255, 252, 253), new Color(255, 225, 238), new Color(246, 190, 216)}));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 78));
            for (int i = 0; i < 10; i++) {
                int x = 28 + (i * 173) % Math.max(1, w - 120);
                int y = 55 + (i * 89) % Math.max(1, h - 190);
                g.fillRoundRect(x, y, 115, 38, 25, 25);
            }
            for (int i = 0; i < 17; i++) {
                int x = 18 + (i * 137) % Math.max(1, w - 30);
                int y = 84 + (i * 111) % Math.max(1, h - 105);
                drawHeart(g, x, y, 8 + i % 5, new Color(238, 125, 172, 80));
            }
            g.setColor(new Color(161, 108, 143));
            g.setFont(new Font("Serif", Font.ITALIC, 23));
            g.drawString("para alguien muy especial", 32, 48);
        }

        private void paintLetterScene(Graphics2D g, int w, int h) {
            float flap = ease(progress / .28f);
            float unfold = ease((progress - .10f) / .58f);
            float text = ease((progress - .67f) / .33f);
            int envelopeW = Math.min(620, w - 80);
            int envelopeH = 190;
            int envelopeX = (w - envelopeW) / 2;
            int envelopeY = h - 218;
            int cardW = envelopeW - 62;
            int cardH = 110 + Math.round(420 * unfold);
            int cardX = (w - cardW) / 2;
            int cardY = envelopeY + 44 - Math.round(390 * unfold);

            paintCardShadow(g, cardX, cardY, cardW, cardH, unfold);
            paintCard(g, cardX, cardY, cardW, cardH, unfold, text);
            paintEnvelope(g, envelopeX, envelopeY, envelopeW, envelopeH, flap);

            if (progress < .03f) {
                String hint = "Toca la pantalla para abrir tu carta";
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                int hintW = g.getFontMetrics().stringWidth(hint) + 38;
                int x = (w - hintW) / 2;
                g.setColor(new Color(255, 253, 255, 222)); g.fillRoundRect(x, h - 55, hintW, 36, 18, 18);
                g.setColor(new Color(201, 99, 149)); g.setStroke(new BasicStroke(1.4f)); g.drawRoundRect(x, h - 55, hintW, 36, 18, 18);
                g.setColor(INK); g.drawString(hint, x + 19, h - 31);
            }
        }

        private void paintCardShadow(Graphics2D g, int x, int y, int w, int h, float unfold) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .18f + unfold * .12f));
            g.setColor(new Color(104, 52, 89)); g.fillRoundRect(x + 10, y + 13, w, h, 30, 30);
            g.setComposite(AlphaComposite.SrcOver);
        }

        private void paintCard(Graphics2D g, int x, int y, int w, int h, float unfold, float textProgress) {
            g.setPaint(new GradientPaint(x, y, new Color(255, 253, 249), x + w, y + h, new Color(255, 233, 244)));
            g.fillRoundRect(x, y, w, h, 30, 30);
            g.setColor(new Color(226, 137, 177)); g.setStroke(new BasicStroke(2.3f)); g.drawRoundRect(x, y, w, h, 30, 30);
            g.setColor(new Color(248, 192, 216, 150)); g.setStroke(new BasicStroke(1.4f));
            g.drawRoundRect(x + 12, y + 12, w - 24, h - 24, 23, 23);

            if (unfold < .12f) return;
            float decorations = Math.min(1f, unfold * 2.5f);
            drawSticker(g, hearts, x + 24, y + 28, 132, 68, decorations);
            drawSticker(g, cookie, x + w - 152, y + 26, 126, 70, decorations);
            drawSticker(g, teddy, x + w - 150, y + h - 108, 120, 75, decorations);
            drawHeart(g, x + 46, y + h - 62, 14, new Color(238, 125, 172, 180));
            drawHeart(g, x + 74, y + h - 82, 8, new Color(156, 192, 229, 190));

            if (textProgress <= 0) return;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textProgress));
            g.setColor(INK); g.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
            String title = "Una cartita para ti";
            g.drawString(title, (getWidth() - g.getFontMetrics().stringWidth(title)) / 2, y + 74);
            g.setFont(new Font("Serif", Font.PLAIN, 19));
            Shape oldClip = g.getClip();
            g.clip(new Rectangle(x + 42, y + 94, w - 195, h - 125));
            int characters = Math.min(message.length(), Math.round(message.length() * textProgress));
            drawWrappedText(g, message.substring(0, characters), x + 44, y + 120, w - 210, 27);
            g.setClip(oldClip);
            g.setComposite(AlphaComposite.SrcOver);
        }

        private void paintEnvelope(Graphics2D g, int x, int y, int w, int h, float flap) {
            Path2D back = new Path2D.Float();
            back.moveTo(x, y + 28); back.quadTo(x + w / 2f, y - 22, x + w, y + 28);
            back.lineTo(x + w, y + h); back.lineTo(x, y + h); back.closePath();
            g.setPaint(new GradientPaint(x, y, new Color(255, 197, 220), x, y + h, new Color(241, 139, 184)));
            g.fill(back); g.setColor(new Color(202, 91, 139)); g.setStroke(new BasicStroke(2.2f)); g.draw(back);

            Path2D front = new Path2D.Float();
            front.moveTo(x, y + 34); front.lineTo(x + w / 2f, y + h * .72f); front.lineTo(x + w, y + 34);
            front.lineTo(x + w, y + h); front.lineTo(x, y + h); front.closePath();
            g.setPaint(new GradientPaint(x, y + 30, new Color(255, 215, 231), x + w, y + h, new Color(244, 157, 194)));
            g.fill(front); g.setColor(new Color(203, 95, 142)); g.draw(front);

            float lift = 92 * flap;
            Path2D cover = new Path2D.Float();
            cover.moveTo(x, y + 36); cover.lineTo(x + w / 2f, y + 128 - lift); cover.lineTo(x + w, y + 36); cover.closePath();
            g.setPaint(new GradientPaint(x, y, new Color(255, 230, 240), x + w, y + 140, new Color(244, 157, 195)));
            g.fill(cover); g.setColor(new Color(203, 95, 142)); g.draw(cover);
            if (flap < .15f) drawSticker(g, hearts, x + w / 2 - 75, y + 64, 150, 70, 1f);
        }

        private void drawSticker(Graphics2D g, BufferedImage image, int x, int y, int w, int h, float opacity) {
            if (image == null || opacity <= 0) return;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(.9f, opacity)));
            g.setPaint(new GradientPaint(x, y, new Color(216, 238, 255), x + w, y + h, new Color(255, 224, 239)));
            g.fillRoundRect(x - 5, y - 5, w + 10, h + 10, 20, 20);
            g.setColor(new Color(150, 192, 226, 150)); g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(x - 5, y - 5, w + 10, h + 10, 20, 20);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, opacity)));
            g.drawImage(image, x, y, w, h, null);
            g.setComposite(AlphaComposite.SrcOver);
        }

        private void drawHeart(Graphics2D g, int x, int y, int size, Color color) {
            AffineTransform old = g.getTransform();
            g.translate(x, y); g.scale(size / 16.0, size / 16.0);
            Path2D heart = new Path2D.Float();
            heart.moveTo(0, 5); heart.curveTo(-17, -7, -14, -23, -4, -19);
            heart.curveTo(0, -17, 0, -12, 0, -9); heart.curveTo(0, -12, 0, -17, 4, -19);
            heart.curveTo(14, -23, 17, -7, 0, 5); heart.closePath();
            g.setColor(color); g.fill(heart); g.setTransform(old);
        }

        private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
            int currentY = y;
            for (String paragraph : text.split("\\R", -1)) {
                if (paragraph.isBlank()) { currentY += lineHeight; continue; }
                StringBuilder line = new StringBuilder();
                for (String word : paragraph.split(" ")) {
                    String candidate = line.isEmpty() ? word : line + " " + word;
                    if (g.getFontMetrics().stringWidth(candidate) > maxWidth && !line.isEmpty()) {
                        g.drawString(line.toString(), x, currentY); currentY += lineHeight; line = new StringBuilder(word);
                    } else line = new StringBuilder(candidate);
                }
                g.drawString(line.toString(), x, currentY); currentY += lineHeight;
            }
        }

        private float ease(float value) {
            float t = Math.max(0f, Math.min(1f, value));
            return t * t * (3f - 2f * t);
        }
    }
}
