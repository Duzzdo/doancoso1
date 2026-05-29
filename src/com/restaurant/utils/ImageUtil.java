package com.restaurant.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Utility class xử lý hình ảnh (upload, resize, convert, display)
 *
 * Chức năng:
 * - Convert BufferedImage ↔ byte[] (để lưu BLOB vào database)
 * - Resize hình ảnh giữ nguyên tỷ lệ (aspect ratio)
 * - Tạo ImageIcon từ byte[] hoặc BufferedImage
 * - Upload hình ảnh từ máy tính (JFileChooser)
 * - Tạo placeholder image khi không có hình
 *
 * Quy trình xử lý hình ảnh:
 * 1. User chọn file từ máy tính (chooseImageFile)
 * 2. Load file thành BufferedImage (loadImageFromFile)
 * 3. Resize hình ảnh (resizeImage) - giảm dung lượng
 * 4. Convert thành byte[] (imageToByteArray)
 * 5. Lưu byte[] vào database (BLOB)
 * 6. Khi hiển thị: byte[] → BufferedImage → ImageIcon
 *
 * Image size constants:
 * - CATEGORY_IMAGE_SIZE = 300px (hình loại món)
 * - FOOD_IMAGE_SIZE = 400px (hình món ăn)
 * - THUMBNAIL_SIZE = 100px (thumbnail trong JTable)
 *
 * Sử dụng trong:
 * - MenuManagementPanel: Upload hình món ăn, loại món
 * - OrderDialog: Hiển thị hình món trong menu
 * - LoaiMonDAO/MonDAO: Convert hình ảnh trước khi insert/update
 */
public class ImageUtil {

    // Image size constants
    public static final int CATEGORY_IMAGE_SIZE = 300; // Kích thước hình loại món (300x300)
    public static final int FOOD_IMAGE_SIZE = 400;     // Kích thước hình món ăn (400x400)
    public static final int THUMBNAIL_SIZE = 100;      // Kích thước thumbnail (100x100)

    /**
     * Convert BufferedImage thành byte[] để lưu BLOB vào database
     *
     * Quy trình:
     * 1. Tạo ByteArrayOutputStream
     * 2. Write BufferedImage vào stream dạng PNG
     * 3. Convert stream thành byte[]
     *
     * Tại sao dùng PNG?
     * - Lossless compression (không mất chất lượng)
     * - Hỗ trợ transparency (trong suốt)
     * - File size nhỏ hơn BMP, lớn hơn JPG
     *
     * @param image BufferedImage cần convert
     * @return byte[] để lưu vào database, null nếu image null hoặc lỗi
     */
    public static byte[] imageToByteArray(BufferedImage image) {
        if (image == null) return null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Error converting image to byte array: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert byte[] thành BufferedImage để hiển thị
     *
     * Quy trình:
     * 1. Tạo ByteArrayInputStream từ byte[]
     * 2. Read stream thành BufferedImage
     *
     * @param bytes byte[] từ database BLOB
     * @return BufferedImage để hiển thị, null nếu bytes null/empty hoặc lỗi
     */
    public static BufferedImage byteArrayToImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(bais);
        } catch (IOException e) {
            System.err.println("Error converting byte array to image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Resize hình ảnh giữ nguyên tỷ lệ (aspect ratio)
     *
     * Quy trình:
     * 1. Tính tỷ lệ width và height
     * 2. Chọn tỷ lệ nhỏ hơn để fit vào khung (không bị crop)
     * 3. Tính kích thước mới
     * 4. Scale hình ảnh với SCALE_SMOOTH (chất lượng cao)
     * 5. Vẽ lên BufferedImage mới
     *
     * Ví dụ:
     * - Original: 800x600, maxWidth: 400, maxHeight: 400
     * - widthRatio = 400/800 = 0.5, heightRatio = 400/600 = 0.67
     * - Chọn ratio = 0.5 (nhỏ hơn)
     * - New size: 400x300 (giữ nguyên tỷ lệ 4:3)
     *
     * @param original BufferedImage gốc
     * @param maxWidth Chiều rộng tối đa
     * @param maxHeight Chiều cao tối đa
     * @return BufferedImage đã resize, null nếu original null
     */
    public static BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        if (original == null) return null;

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // Calculate new dimensions maintaining aspect ratio
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        Image tmp = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }

    /**
     * Create ImageIcon from byte array with resize
     */
    public static ImageIcon createImageIcon(byte[] bytes, int width, int height) {
        BufferedImage image = byteArrayToImage(bytes);
        if (image == null) {
            return getDefaultImageIcon(width, height);
        }

        BufferedImage resized = resizeImage(image, width, height);
        return new ImageIcon(resized);
    }

    /**
     * Create ImageIcon from BufferedImage with resize
     */
    public static ImageIcon createImageIcon(BufferedImage image, int width, int height) {
        if (image == null) {
            return getDefaultImageIcon(width, height);
        }

        BufferedImage resized = resizeImage(image, width, height);
        return new ImageIcon(resized);
    }

    /**
     * Get default placeholder image
     */
    public static ImageIcon getDefaultImageIcon(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Gray background
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);

        // Draw "No Image" text
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String text = "No Image";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height + fm.getAscent()) / 2;
        g2d.drawString(text, x, y);

        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * Mở JFileChooser để user chọn hình ảnh từ máy tính
     *
     * File filter:
     * - Chỉ chấp nhận: .jpg, .jpeg, .png, .gif
     * - Hiển thị cả thư mục để dễ navigate
     *
     * Sử dụng khi: User click button "Chọn hình ảnh" trong MenuManagementPanel
     *
     * @param parent Component cha (để center dialog)
     * @return BufferedImage đã load, null nếu user cancel hoặc lỗi
     */
    public static BufferedImage chooseImageFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn hình ảnh");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                       name.endsWith(".png") || name.endsWith(".gif");
            }

            @Override
            public String getDescription() {
                return "Image Files (*.jpg, *.jpeg, *.png, *.gif)";
            }
        });

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return loadImageFromFile(fileChooser.getSelectedFile());
        }
        return null;
    }

    /**
     * Load image from file
     */
    public static BufferedImage loadImageFromFile(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Error loading image from file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get default category image
     */
    public static BufferedImage getDefaultCategoryImage() {
        return createDefaultImage(CATEGORY_IMAGE_SIZE, CATEGORY_IMAGE_SIZE, "Category");
    }

    /**
     * Get default food image
     */
    public static BufferedImage getDefaultFoodImage() {
        return createDefaultImage(FOOD_IMAGE_SIZE, FOOD_IMAGE_SIZE, "Food");
    }

    /**
     * Create default placeholder image with text
     */
    private static BufferedImage createDefaultImage(int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Gray background
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);

        // Draw text
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height + fm.getAscent()) / 2;
        g2d.drawString(text, x, y);

        g2d.dispose();
        return image;
    }
}
