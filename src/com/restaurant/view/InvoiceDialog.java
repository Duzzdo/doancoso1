package com.restaurant.view;

import com.restaurant.dao.ChiTietDonDatDAO;
import com.restaurant.dao.NhanVienDAO;
import com.restaurant.dao.BanAnDAO;
import com.restaurant.model.BanAnDTO;
import com.restaurant.model.ChiTietDonDatDTO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.model.NhanVienDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.print.*;
import javax.print.PrintService;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Dialog hiển thị hóa đơn (chỉ xem, không chỉnh sửa)
 * Giao diện: Header + Thông tin đơn + Danh sách món + Tổng tiền + Footer
 */
public class InvoiceDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private DonDatDTO donDat;
    private ChiTietDonDatDAO chiTietDonDatDAO;
    private NhanVienDAO nhanVienDAO;
    private BanAnDAO banAnDAO;
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    private static final Color HEADER_BG = new Color(220, 53, 69);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    /**
     * Constructor: Khởi tạo dialog hóa đơn
     */
    public InvoiceDialog(Frame parent, DonDatDTO donDat) {
        super(parent, "Hóa đơn #" + donDat.getMaDonDat(), true);        // Modal dialog
        this.donDat = donDat;
        this.chiTietDonDatDAO = new ChiTietDonDatDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.banAnDAO = new BanAnDAO();

        initComponents();
        setSize(450, 650);
        setLocationRelativeTo(parent);                                   // Hiển thị giữa màn hình
        setVisible(true);
    }

    /**
     * Khởi tạo các components của dialog
     */
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));                               // BorderLayout: Content (CENTER) + Buttons (SOUTH)
        getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // BoxLayout dọc
        contentPanel.setBackground(Color.WHITE);

        // Thêm các sections theo thứ tự
        contentPanel.add(createHeaderPanel());                           // Header: Logo + Tên nhà hàng
        contentPanel.add(Box.createVerticalStrut(15));                   // Khoảng cách 15px

        contentPanel.add(createSeparatorLine());                         // Đường kẻ ngang
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createTitlePanel());                            // Title: "HÓA ĐƠN"
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createSeparatorLine());
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createOrderInfoPanel());                        // Thông tin đơn hàng
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createItemsTablePanel());                       // Danh sách món
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createTotalPanel());                            // Tổng tiền
        contentPanel.add(Box.createVerticalStrut(20));

        contentPanel.add(createFooterPanel());                           // Footer: Cảm ơn

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);                             // Thêm main panel vào center

        add(createButtonPanel(), BorderLayout.SOUTH);                    // Thêm button panel vào bottom
    }

    /**
     * Tạo đường kẻ ngang phân cách
     */
    private JSeparator createSeparatorLine() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL); // Đường kẻ ngang
        separator.setForeground(BORDER_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));   // Height 1px, width full
        return separator;
    }

    /**
     * Tạo panel header (tên nhà hàng, địa chỉ, SĐT)
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));        // BoxLayout dọc
        panel.setBackground(Color.WHITE);

        JLabel lblRestaurant = new JLabel("NHÀ HÀNG HƯƠNG VỊ VIỆT", SwingConstants.CENTER);
        lblRestaurant.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblRestaurant.setForeground(HEADER_BG);
        lblRestaurant.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAddress = new JLabel("Địa chỉ: 140 Trần Đại Nghĩa, phường Ngũ Hành Sơn, thành phố Đà Nẵng", SwingConstants.CENTER);
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAddress.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPhone = new JLabel("Điện thoại: 0905100200", SwingConstants.CENTER);
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPhone.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblRestaurant);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblAddress);
        panel.add(Box.createVerticalStrut(3));
        panel.add(lblPhone);

        return panel;
    }

    /**
     * Tạo panel title (tiêu đề "HÓA ĐƠN" + mã đơn)
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));        // BoxLayout dọc
        panel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("HÓA ĐƠN KHÁCH HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblOrderId = new JLabel("ID NGẪU ĐƠN: " + donDat.getMaDonDat(), SwingConstants.CENTER);
        lblOrderId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblOrderId.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblOrderId);

        return panel;
    }

    /**
     * Tạo panel thông tin đơn hàng (bàn, nhân viên)
     */
    private JPanel createOrderInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));        // BoxLayout dọc
        panel.setBackground(Color.WHITE);

        BanAnDTO ban = banAnDAO.getBanAnById(donDat.getMaBan());        // Lấy thông tin bàn
        NhanVienDTO nhanVien = nhanVienDAO.getNhanVienById(donDat.getMaNV()); // Lấy thông tin nhân viên

        JLabel lblTable = new JLabel("Bàn: " + (ban != null ? ban.getTenBan() : "N/A"));
        lblTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTable.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStaff = new JLabel("Nhân viên: " + (nhanVien != null ? nhanVien.getHoTenNV() : "N/A"));
        lblStaff.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStaff.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTable);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblStaff);

        return panel;
    }

    /**
     * Tạo panel danh sách món (table)
     */
    private JPanel createItemsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());                  // BorderLayout: Table (CENTER)
        panel.setBackground(Color.WHITE);

        String[] columns = {"Tên món", "SL", "Đơn giá", "Thành tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                                            // Table không cho edit
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemsTable.setRowHeight(30);
        itemsTable.setShowGrid(true);
        itemsTable.setGridColor(BORDER_COLOR);
        itemsTable.setIntercellSpacing(new Dimension(1, 1));

        itemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        itemsTable.getTableHeader().setBackground(Color.WHITE);
        itemsTable.getTableHeader().setForeground(Color.BLACK);
        itemsTable.getTableHeader().setPreferredSize(new Dimension(0, 35));

        // Căn giữa cột "SL"
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        itemsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Căn phải các cột số (Đơn giá, Thành tiền)
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        itemsTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Set độ rộng các cột
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        // Load danh sách món từ database
        List<ChiTietDonDatDTO> items = chiTietDonDatDAO.getChiTietByDonDat(donDat.getMaDonDat());
        for (ChiTietDonDatDTO item : items) {                            // Duyệt qua từng món
            tableModel.addRow(new Object[]{
                item.getTenMon(),
                item.getSoLuong(),
                String.format("%,d", (int)item.getGiaTien()),           // Format: 50,000
                String.format("%,d", (int)item.getThanhTien())
            });
        }

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setPreferredSize(new Dimension(0, Math.min(200, items.size() * 30 + 35))); // Height tự động theo số món
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo panel tổng tiền
     */
    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // FlowLayout ngang, căn trái
        panel.setBackground(Color.WHITE);

        JLabel lblTotal = new JLabel("TỔNG TIỀN: " + String.format("%,d VNĐ", (int)donDat.getTongTien())); // Format: 150,000 VNĐ
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));

        panel.add(lblTotal);

        return panel;
    }

    /**
     * Tạo panel footer (cảm ơn, ngày in, ghi chú)
     */
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));        // BoxLayout dọc
        panel.setBackground(Color.WHITE);

        JLabel lblThankYou = new JLabel("XIN CẢM ƠN QUÝ KHÁCH", SwingConstants.CENTER);
        lblThankYou.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThankYou.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDate = new JLabel("Ngày in: " + donDat.getNgayDat(), SwingConstants.CENTER);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNote = new JLabel("HÓA ĐƠN chỉ ghi lại trong ngày", SwingConstants.CENTER);
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblNote.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblThankYou);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblDate);
        panel.add(Box.createVerticalStrut(3));
        panel.add(lblNote);

        return panel;
    }

    /**
     * Tạo panel buttons (In hóa đơn, Đóng)
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15)); // FlowLayout ngang cho 2 buttons
        panel.setBackground(Color.WHITE);

        JButton btnPrint = new JButton("In hóa đơn");
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPrint.setPreferredSize(new Dimension(160, 45));
        btnPrint.setBackground(new Color(52, 152, 219));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setBorderPainted(false);
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrint.addActionListener(e -> printInvoice());                 // In hóa đơn ra PDF

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(160, 45));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());                      // Đóng dialog

        panel.add(btnPrint);
        panel.add(btnClose);

        return panel;
    }

    /**
     * Xử lý in hóa đơn ra file PDF
     */
    private void printInvoice() {
        JFileChooser fileChooser = new JFileChooser();                   // Mở file chooser để chọn nơi lưu
        fileChooser.setDialogTitle("Lưu hóa đơn PDF");

        // Tạo tên file với timestamp để tránh trùng
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        fileChooser.setSelectedFile(new File("HoaDon_" + donDat.getMaDonDat() + "_" + timestamp + ".pdf"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {             // Nếu user chọn nơi lưu
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            // Add .pdf extension if not present
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
                fileToSave = new File(filePath);
            }

            // Try to use printer if available, otherwise show message
            PrinterJob job = PrinterJob.getPrinterJob();
            PrintService[] printServices = PrinterJob.lookupPrintServices();

            if (printServices.length == 0) {
                JOptionPane.showMessageDialog(this,
                    "Không tìm thấy máy in.\n" +
                    "Vui lòng cài đặt 'Microsoft Print to PDF' từ Windows Settings:\n" +
                    "Settings → Apps → Optional features → Add a feature → Microsoft Print to PDF\n\n" +
                    "Sau đó thử lại để lưu hóa đơn thành file PDF.",
                    "Cần cài đặt máy in PDF",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Find PDF printer
            PrintService pdfPrinter = null;
            for (PrintService service : printServices) {
                String printerName = service.getName().toLowerCase();
                if (printerName.contains("pdf") || printerName.contains("adobe")) {
                    pdfPrinter = service;
                    break;
                }
            }

            // If no PDF printer, find any printer except OneNote
            if (pdfPrinter == null) {
                for (PrintService service : printServices) {
                    String printerName = service.getName().toLowerCase();
                    if (!printerName.contains("onenote") && !printerName.contains("xps") && !printerName.contains("fax")) {
                        pdfPrinter = service;
                        break;
                    }
                }
            }

            if (pdfPrinter != null) {
                try {
                    job.setPrintService(pdfPrinter);
                    job.setPrintable(new InvoicePrintable());

                    if (job.printDialog()) {
                        job.print();
                        JOptionPane.showMessageDialog(this,
                            "Đã gửi hóa đơn đến máy in!\n\n" +
                            "LƯU Ý: Nếu gặp lỗi 'file being used':\n" +
                            "- Đóng tất cả file PDF đang mở\n" +
                            "- Hoặc chọn tên file khác khi lưu\n\n" +
                            "File được đề xuất: " + fileToSave.getName(),
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (PrinterException e) {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("being used")) {
                        JOptionPane.showMessageDialog(this,
                            "Lỗi: File đang được sử dụng bởi chương trình khác!\n\n" +
                            "Giải pháp:\n" +
                            "1. Đóng tất cả file PDF đang mở\n" +
                            "2. Thử lại và chọn tên file khác\n" +
                            "3. Hoặc lưu vào thư mục khác",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Lỗi khi in: " + errorMsg,
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Không tìm thấy máy in phù hợp.\n" +
                    "Vui lòng cài đặt 'Microsoft Print to PDF' từ Windows Settings.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Inner class for printing
    private class InvoicePrintable implements Printable {
        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Calculate page width and margins
            int pageWidth = (int) pageFormat.getImageableWidth();
            int leftMargin = 40;
            int rightMargin = pageWidth - 40;
            int centerX = pageWidth / 2;

            int y = 50;
            int lineHeight = 20;

            // Header - Restaurant name (centered)
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String restaurantName = "NHÀ HÀNG HƯƠNG VỊ VIỆT";
            int nameWidth = g2d.getFontMetrics().stringWidth(restaurantName);
            g2d.drawString(restaurantName, centerX - nameWidth / 2, y);
            y += lineHeight;

            // Address (centered)
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String address = "Địa chỉ: 140 Trần Đại Nghĩa, phường Ngũ Hành Sơn, thành phố Đà Nẵng";
            int addressWidth = g2d.getFontMetrics().stringWidth(address);
            g2d.drawString(address, centerX - addressWidth / 2, y);
            y += lineHeight - 5;

            // Phone (centered)
            String phone = "Điện thoại: 0905100200";
            int phoneWidth = g2d.getFontMetrics().stringWidth(phone);
            g2d.drawString(phone, centerX - phoneWidth / 2, y);
            y += lineHeight + 5;

            // Separator line
            g2d.drawLine(leftMargin, y, rightMargin, y);
            y += 20;

            // Invoice title (centered)
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String invoiceTitle = "HÓA ĐƠN KHÁCH HÀNG";
            int titleWidth = g2d.getFontMetrics().stringWidth(invoiceTitle);
            g2d.drawString(invoiceTitle, centerX - titleWidth / 2, y);
            y += lineHeight;

            // Invoice ID (centered)
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String invoiceId = "ID NGẪU ĐƠN: " + donDat.getMaDonDat();
            int idWidth = g2d.getFontMetrics().stringWidth(invoiceId);
            g2d.drawString(invoiceId, centerX - idWidth / 2, y);
            y += lineHeight + 5;

            // Separator line
            g2d.drawLine(leftMargin, y, rightMargin, y);
            y += 20;

            // Order info
            BanAnDTO ban = banAnDAO.getBanAnById(donDat.getMaBan());
            NhanVienDTO nhanVien = nhanVienDAO.getNhanVienById(donDat.getMaNV());

            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.drawString("Bàn: " + (ban != null ? ban.getTenBan() : "N/A"), leftMargin, y);
            y += lineHeight;
            g2d.drawString("Nhân viên: " + (nhanVien != null ? nhanVien.getHoTenNV() : "N/A"), leftMargin, y);
            y += lineHeight + 10;

            // Table header
            int col1 = leftMargin;
            int col2 = leftMargin + (pageWidth - leftMargin * 2) * 60 / 100;  // 60% for item name
            int col3 = leftMargin + (pageWidth - leftMargin * 2) * 70 / 100;  // 70% for quantity
            int col4 = leftMargin + (pageWidth - leftMargin * 2) * 80 / 100;  // 80% for price
            int col5 = leftMargin + (pageWidth - leftMargin * 2) * 92 / 100;  // 92% for total

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2d.drawString("Tên món", col1, y);
            g2d.drawString("SL", col2, y);
            g2d.drawString("Đơn giá", col3, y);
            g2d.drawString("Thành tiền", col4, y);
            y += lineHeight;

            g2d.drawLine(leftMargin, y, rightMargin, y);
            y += 15;

            // Items
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            List<ChiTietDonDatDTO> items = chiTietDonDatDAO.getChiTietByDonDat(donDat.getMaDonDat());
            for (ChiTietDonDatDTO item : items) {
                g2d.drawString(item.getTenMon(), col1, y);
                g2d.drawString(String.valueOf(item.getSoLuong()), col2, y);
                g2d.drawString(String.format("%,d", (int)item.getGiaTien()), col3, y);
                g2d.drawString(String.format("%,d", (int)item.getThanhTien()), col4, y);
                y += lineHeight;
            }

            y += 10;
            g2d.drawLine(leftMargin, y, rightMargin, y);
            y += 20;

            // Total
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2d.drawString("TỔNG TIỀN: " + String.format("%,d VNĐ", (int)donDat.getTongTien()), leftMargin, y);
            y += lineHeight + 20;

            // Footer - Thank you message (centered)
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String thankYou = "XIN CẢM ƠN QUÝ KHÁCH";
            int thankYouWidth = g2d.getFontMetrics().stringWidth(thankYou);
            g2d.drawString(thankYou, centerX - thankYouWidth / 2, y);
            y += lineHeight;

            // Print date (centered)
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String printDate = "Ngày in: " + donDat.getNgayDat();
            int dateWidth = g2d.getFontMetrics().stringWidth(printDate);
            g2d.drawString(printDate, centerX - dateWidth / 2, y);
            y += lineHeight;

            // Note (centered, italic)
            g2d.setFont(new Font("Segoe UI", Font.ITALIC, 9));
            String note = "HÓA ĐƠN chỉ ghi lại trong ngày";
            int noteWidth = g2d.getFontMetrics().stringWidth(note);
            g2d.drawString(note, centerX - noteWidth / 2, y);
            y += lineHeight + 10;

            // Barcode
            drawBarcode(g2d, centerX, y, donDat.getMaDonDat());

            return PAGE_EXISTS;
        }

        private void drawBarcode(Graphics2D g2d, int centerX, int y, int orderId) {
            // Generate barcode string from order ID
            String barcodeData = String.format("INV%06d", orderId);

            // Barcode dimensions
            int barWidth = 2;
            int barHeight = 40;
            int totalWidth = barcodeData.length() * 12 * barWidth;
            int startX = centerX - totalWidth / 2;

            // Simple barcode pattern (alternating bars)
            g2d.setColor(Color.BLACK);
            int x = startX;
            for (int i = 0; i < barcodeData.length(); i++) {
                char c = barcodeData.charAt(i);
                int charValue = Character.isDigit(c) ? (c - '0') : (c - 'A' + 10);

                // Draw bars based on character value
                for (int j = 0; j < 6; j++) {
                    if ((charValue & (1 << j)) != 0) {
                        g2d.fillRect(x, y, barWidth, barHeight);
                    }
                    x += barWidth * 2;
                }
            }

            // Draw barcode text below
            g2d.setFont(new Font("Courier New", Font.PLAIN, 10));
            int textWidth = g2d.getFontMetrics().stringWidth(barcodeData);
            g2d.drawString(barcodeData, centerX - textWidth / 2, y + barHeight + 15);
        }
    }
}
