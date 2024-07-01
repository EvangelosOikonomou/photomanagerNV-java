import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class MainForm extends JFrame {
    private JPanel mainPanel;
    private JTextField filePathField;
    private JButton browseButton;
    private JPanel metadataPanel;
    private JButton addButton;
    private JTable photoTable;
    private JComboBox<String> profileComboBox;
    private JButton addProfileButton;
    private JButton bulkUploadButton;
    private JScrollPane scrollPane;
    private JLabel photoLabel;
    private JPanel photoGalleryPanel;
    private JButton showXMLButton;
    private JTextArea txtAreaXmlContent;
    private JTextField searchField;
    private JButton searchButton;
    private JButton addMetadataFieldButton;
    private JTextField textField1;
    private final PhotoManager photoManager;
    private final List<JTextField> metadataFields;

    public MainForm() {
        photoManager = new PhotoManager();
        metadataFields = new ArrayList<>();

        setTitle("Photo Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        setVisible(true);

        // Ρύθμιση του metadataPanel να χρησιμοποιεί BoxLayout
        metadataPanel.setLayout(new BoxLayout(metadataPanel, BoxLayout.Y_AXIS));

        // Αρχικοποίηση JComboBox με τα προφίλ χρηστών
        profileComboBox.addItem("Doctor");
        profileComboBox.addItem("Accountant");
        profileComboBox.addItem("Personal");

        profileComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMetadataFields(profileComboBox.getSelectedItem().toString());
            }
        });

        addProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newProfile = JOptionPane.showInputDialog("Enter new profile name:");
                if (newProfile != null && !newProfile.trim().isEmpty()) {
                    profileComboBox.addItem(newProfile);
                    profileComboBox.setSelectedItem(newProfile);
                }
            }
        });

        // Προσθήκη αρχικού πεδίου metadata
        addMetadataField();

        addMetadataFieldButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMetadataField();
            }
        });

        // Ρύθμιση του photoGalleryPanel να χρησιμοποιεί GridLayout
        photoGalleryPanel.setLayout(new GridLayout(0, 3, 10, 10)); // 3 στήλες με 10px διάκενο

        // Δημιουργία και ορισμός JTextArea μέσα στο JScrollPane
        txtAreaXmlContent = new JTextArea();
        txtAreaXmlContent.setEditable(false);
        scrollPane.setViewportView(txtAreaXmlContent);

        // Action listener για το browse button
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                    displayImage(selectedFile);
                }
            }
        });

        // Action listener για το add button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = filePathField.getText();
                List<String> metadataList = new ArrayList<>();
                for (JTextField field : metadataFields) {
                    metadataList.add(field.getText());
                }
                String metadata = String.join(", ", metadataList);
                Photo photo = new Photo(filePath, metadata);
                photoManager.addPhoto(photo);
                addPhotoToGallery(photo);
                updateTable();
                saveMetadataToXML(photo);
            }
        });

        // Action listener για το bulk upload button
        bulkUploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    List<String> metadataList = new ArrayList<>();
                    for (JTextField field : metadataFields) {
                        metadataList.add(field.getText());
                    }
                    String metadata = String.join(", ", metadataList);

                    for (File file : selectedFiles) {
                        Photo photo = new Photo(file.getAbsolutePath(), metadata);
                        photoManager.addPhoto(photo);
                        addPhotoToGallery(photo);
                        saveMetadataToXML(photo);
                    }
                    updateTable();
                }
            }
        });

        // Action listener για το show XML button
        showXMLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showXMLContent();
            }
        });

        // Action listener για το search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText().trim();
                filterPhotosByMetadata(query);
            }
        });
        metadataPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
            }
        });
    }

    private void displayImage(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            Image scaledImage = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaledImage);
            photoLabel.setIcon(icon);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading image", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPhotoToGallery(Photo photo) {
        try {
            BufferedImage img = ImageIO.read(new File(photo.getFilePath()));
            Image scaledImage = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Μικρό μέγεθος για την gallery
            ImageIcon icon = new ImageIcon(scaledImage);
            JLabel photoLabel = new JLabel(icon);
            photoGalleryPanel.add(photoLabel);
            photoGalleryPanel.revalidate();
            photoGalleryPanel.repaint();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading image", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable() {
        String[] columnNames = {"File Path", "Metadata"};
        Object[][] data = photoManager.getPhotosData();
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        photoTable.setModel(model);
    }

    private void saveMetadataToXML(Photo photo) {
        try {
            File xmlFile = new File("metadata.xml");
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document;
            Element root;

            // Αν το αρχείο XML υπάρχει ήδη, φόρτωσε το υπάρχον αρχείο
            if (xmlFile.exists()) {
                document = documentBuilder.parse(xmlFile);
                root = document.getDocumentElement();
            } else {
                document = documentBuilder.newDocument();
                root = document.createElement("photos");
                document.appendChild(root);
            }

            // Δημιουργία νέου στοιχείου φωτογραφίας
            Element photoElement = document.createElement("photo");
            addXmlElement(document, photoElement, "path", photo.getFilePath());
            addXmlElement(document, photoElement, "metadata", photo.getMetadata());

            root.appendChild(photoElement);

            // Αποθήκευση του τροποποιημένου XML αρχείου
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(xmlFile);
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving metadata to XML", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing XML file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addXmlElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
    }

    private void showXMLContent() {
        try {
            File xmlFile = new File("metadata.xml");
            if (xmlFile.exists()) {
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(xmlFile);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter writer = new StringWriter();
                DOMSource domSource = new DOMSource(document);
                StreamResult result = new StreamResult(writer);
                transformer.transform(domSource, result);

                txtAreaXmlContent.setText(writer.toString());
            } else {
                txtAreaXmlContent.setText("No XML file found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error displaying XML content", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterPhotosByMetadata(String query) {
        photoGalleryPanel.removeAll();
        String[] searchTerms = query.split(","); // Διαχωρισμός των όρων αναζήτησης με βάση το κόμμα
        List<Photo> filteredPhotos = photoManager.getPhotosByMetadata(searchTerms);
        for (Photo photo : filteredPhotos) {
            addPhotoToGallery(photo);
        }
        photoGalleryPanel.revalidate();
        photoGalleryPanel.repaint();
    }

    private void addMetadataField() {
        JTextField metadataField = new JTextField(20);
        metadataFields.add(metadataField);
        metadataPanel.add(metadataField);
        metadataPanel.revalidate();
        metadataPanel.repaint();
    }

    private void updateMetadataFields(String profile) {
        metadataPanel.removeAll();
        metadataFields.clear();
        addMetadataField();  // Προσθήκη αρχικού πεδίου metadata

        switch (profile) {
            case "Doctor":
                addMetadataField();
                addMetadataField();
                addMetadataField();
                addMetadataField();
                break;
            case "Accountant":
                addMetadataField();
                addMetadataField();
                addMetadataField();
                addMetadataField();
                break;
            case "Personal":
                addMetadataField();
                addMetadataField();
                addMetadataField();
                addMetadataField();
                addMetadataField();
                break;
            default:
                addMetadataField();  // Προσθήκη αρχικού πεδίου metadata για προσαρμοσμένα προφίλ
                break;
        }

        metadataPanel.revalidate();
        metadataPanel.repaint();
    }

    public static void main(String[] args) {
        new MainForm();
    }
}

