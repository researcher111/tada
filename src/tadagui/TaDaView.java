/*
 * ProjectFinal1View.java
 */

package tadagui;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;

import tadadriver.CoverageDatabaseAttributeMapping;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;

import java.io.File;
import java.lang.Object;
import javax.swing.table.DefaultTableModel;



/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
/**
 * The application's main frame.
 */
public class TaDaView extends FrameView {

    public TaDaView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = TaDaApp.getApplication().getMainFrame();
            aboutBox = new ProjectFinal1AboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        TaDaApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        GroupLayout jPanel1Layout = new GroupLayout((JComponent)jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList3 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        settingsPanel =new JPanel();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = AbstractComboBoxModel.listTables();
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setName("jList1"); // NOI18N
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jList2.setName("jList2"); // NOI18N
        jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList2ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList2);

        jScrollPane3.setName("jScrollPane3"); // NOI18N
        {
        	BrowseForApplication = new JFormattedTextField();
        	BrowseForApplication.setName("BrowseForApplication");
        }

        jList3.setName("jList3"); // NOI18N
        jList3.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList3ValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jList3);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(TaDaApp.class).getContext().getResourceMap(TaDaView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        ; // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        {
        	ApplicationPath = new JTextField();
        	ApplicationPath.setName("ApplicationPath");
        }
        {
        	jButton2 = new JButton();
        	jButton2.setName("jButton2");
        	jButton2.addMouseListener(new MouseAdapter() {
        		public void mouseClicked(MouseEvent evt) {
        			jButton2MouseClicked(evt);
        		}
        	});
        }
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createSequentialGroup()
        	.addComponent(BrowseForApplication, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
        	.addGap(30)
        	.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	    .addComponent(jLabel1, GroupLayout.Alignment.BASELINE, 0, 19, Short.MAX_VALUE)
        	    .addComponent(jLabel2, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jLabel3, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
        	.addGap(32)
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 276, GroupLayout.PREFERRED_SIZE)
        	        .addGap(6))
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 276, GroupLayout.PREFERRED_SIZE)
        	        .addGap(6))
        	    .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE))
        	.addGap(39)
        	.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	    .addComponent(jButton2, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(ApplicationPath, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
        	.addGap(17)
        	.addComponent(jButton1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	.addContainerGap(17, 17));
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createSequentialGroup()
        	.addComponent(BrowseForApplication, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
        	.addGap(35)
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)
        	        .addGap(0, 35, Short.MAX_VALUE)
        	        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addGap(55))
        	    .addGroup(jPanel1Layout.createSequentialGroup()
        	        .addGap(42)
        	        .addGroup(jPanel1Layout.createParallelGroup()
        	            .addComponent(ApplicationPath, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
        	            .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
        	                .addGap(119)))
        	        .addGap(26)
        	        .addGroup(jPanel1Layout.createParallelGroup()
        	            .addGroup(jPanel1Layout.createSequentialGroup()
        	                .addGap(0, 0, Short.MAX_VALUE)
        	                .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE))
        	            .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	                .addPreferredGap(jScrollPane2, jButton2, LayoutStyle.ComponentPlacement.INDENT)
        	                .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
        	                .addGap(44)))))
        	.addGap(107)
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addComponent(jScrollPane3, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE)
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addGap(44)
        	        .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addGap(48)))
        	.addContainerGap(334, 334));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(764, 478));

        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane4.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 724, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(272, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        
        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable2.setName("jTable2"); // NOI18N
        jScrollPane5.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(275, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(72, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N
        
        

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable3.setName("jTable3"); // NOI18N
        jScrollPane6.setViewportView(jTable3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(183, 183, 183)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(371, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        
        jTabbedPane1.addTab(resourceMap.getString("settingsPanel.TabConstraints.tabTitle"), settingsPanel); // NOI18N
        settingsPanel.setName("settingsPanel"); // NOI18N
        {
        	sootCommandLabel = new JLabel();
        	settingsPanel.add(sootCommandLabel);
        	sootCommandLabel.setName("sootCommandLabel");
        }
        {
        	sootCommand = new JTextArea();
        	sootCommand.setLineWrap(true);
        	//sootCommand.set
        	settingsPanel.add(sootCommand);
        	sootCommand.setName("sootCommand");
        	sootCommand.setPreferredSize(new java.awt.Dimension(839, 369));
        	//unix usage
        	//sootCommand.setText("--interactive-mode  --d  \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\sootOutput\" --cp \"/C:/Program\\ Files/Java/jre6/lib/jce.jar;/C:/Program\\ Files/Java/jre6/lib/charsets.jar;/C:/Program\\ Files/Java/jre6/lib/ext/dnsns.jar;/C:/Program\\ Files/Java/jre6/lib/jsse.jar;/C:/Sample/src;/C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;/C:/Program\\ Files/Java/jre6/lib/resources.jar;/C:/Documents\\ and\\ Settings/kunal_taneja/workspace/Sample/bin/;/C:/Program\\ Files/Java/jre6/lib/ext/localedata.jar;/C:/Program\\ Files/Java/jre6/lib/rt.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;;C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\src;C:\\Program\\ Files\\Java\\jre6\\lib\\resources.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\rt.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jsse.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jce.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\charsets.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/DurboDax/src;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/UnixUsage/src;C:/Documents\\ and\\ Settings/kunal_taneja/GlassFish_v3_Prelude/glassfish/modules/javax.servlet.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\UnixUsage\\src\" app.CourseInfo");
        	//RiskInsurance
        	//sootCommand.setText("--interactive-mode  --d  \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\sootOutput\" --cp \"/C:/Program\\ Files/Java/jre6/lib/jce.jar;/C:/Program\\ Files/Java/jre6/lib/charsets.jar;/C:/Program\\ Files/Java/jre6/lib/ext/dnsns.jar;/C:/Program\\ Files/Java/jre6/lib/jsse.jar;/C:/Sample/src;/C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;/C:/Program\\ Files/Java/jre6/lib/resources.jar;/C:/Documents\\ and\\ Settings/kunal_taneja/workspace/Sample/bin/;/C:/Program\\ Files/Java/jre6/lib/ext/localedata.jar;/C:/Program\\ Files/Java/jre6/lib/rt.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;;C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\src;C:\\Program\\ Files\\Java\\jre6\\lib\\resources.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\rt.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jsse.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jce.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\charsets.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/DurboDax/src;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/UnixUsage/src;C:/Documents\\ and\\ Settings/kunal_taneja/GlassFish_v3_Prelude/glassfish/modules/javax.servlet.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\RiskInsurance\\src\" com.riskIt.app.MainClass");
        	//Durbodax
        	sootCommand.setText("--interactive-mode  --d  \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\sootOutput\" --cp \"/C:/Program\\ Files/Java/jre6/lib/jce.jar;/C:/Program\\ Files/Java/jre6/lib/charsets.jar;/C:/Program\\ Files/Java/jre6/lib/ext/dnsns.jar;/C:/Program\\ Files/Java/jre6/lib/jsse.jar;/C:/Sample/src;/C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;/C:/Program\\ Files/Java/jre6/lib/resources.jar;/C:/Documents\\ and\\ Settings/kunal_taneja/workspace/Sample/bin/;/C:/Program\\ Files/Java/jre6/lib/ext/localedata.jar;/C:/Program\\ Files/Java/jre6/lib/rt.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;;C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\src;C:\\Program\\ Files\\Java\\jre6\\lib\\resources.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\rt.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jsse.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jce.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\charsets.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/DurboDax/src;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/UnixUsage/src;C:/Documents\\ and\\ Settings/kunal_taneja/GlassFish_v3_Prelude/glassfish/modules/javax.servlet.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\RiskInsurance\\src\" durbodax.Main");
        }

        mainPanel.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 19, -1, -1));

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(TaDaApp.class).getContext().getActionMap(TaDaView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 615, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jList3ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList3ValueChanged
        // TODO add your handling code here:
        fkKey = (String)jList3.getSelectedValue();
        System.out.println("You have selected this table "+fkKey);
        AbstractComboBoxModel.getForeignValues();
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
                AbstractComboBoxModel.foreignData, AbstractComboBoxModel.foreignColumnNames
                ));
}//GEN-LAST:event_jList3ValueChanged

    private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList2ValueChanged
        // TODO add your handling code here:
        selectedCol = (String)jList2.getSelectedValue();
        System.out.println("You have selected this table"+selectedCol);
        //ArrayList colNames = AbstractComboBoxModel.processTableValue();
        jList3.setModel(new javax.swing.AbstractListModel() {
            String[] strings = AbstractComboBoxModel.processAttributeValue();
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
}//GEN-LAST:event_jList2ValueChanged

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        // TODO add your handling code here:
        selectedTable = (String)jList1.getSelectedValue();
        System.out.println("You have selected this table "+selectedTable);
        //ArrayList colNames = AbstractComboBoxModel.processTableValue();
        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = AbstractComboBoxModel.processTableValue();
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        AbstractComboBoxModel.TableFromDatabase();
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                AbstractComboBoxModel.data, AbstractComboBoxModel.columnNames
                ));


     
}//GEN-LAST:event_jList1ValueChanged

    private void jButton1MouseClicked(@SuppressWarnings("unused") java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO add your handling code here:
    	
    	ArrayList<String> arguments = new ArrayList<String>();
    	String txt = sootCommand.getText();
    	String[] args = txt.split("\"");
    	for (String string : args) {
			if (string.split("[^\\\\][\\s]").length == 1){
				string = string.replace("\\ ", " ");
				arguments.add(string.trim());
			}
			else
				for(String s : string.split("[\\s]"))
					if(s.trim().length() >0 )
						arguments.add(s.trim());
		}
    	String[] str = new String[arguments.size()];
    	int i=0;
    	for (String string : arguments) {
			str[i++] = string;
		}
    	tadadriver.TaDaMain.main(str);
    	
    	
//    	CoverageDatabaseAttributeMapping CAM = new CoverageDatabaseAttributeMapping();
//		CAM.addCoverageFor("A", 1);
//		CAM.addCoverageFor("B", 2);
//		CAM.addCoverageFor("C", 3);
//		CAM.addCoverageFor("D", 4);
//		CAM.addCoverageFor("E", 5);
//    	CAM.addCoverageFor("F", 6);
//
//    	CoverageDatabaseAttributeMapping.addCoverageFor("Emp", "AA", 10);
//    	CoverageDatabaseAttributeMapping.addCoverageFor("Emp", "AA", 10);
//    	CoverageDatabaseAttributeMapping.addCoverageFor("Emp", "AB", 20);
//	   	CoverageDatabaseAttributeMapping.addCoverageFor("Emp", "AC", 30);
//	   	CoverageDatabaseAttributeMapping.addCoverageFor("Emp", "AD", 40);
//	   	CoverageDatabaseAttributeMapping.addCoverageFor("Emp", "AE", 50);

      DefaultTableModel model = new DefaultTableModel();
      // Populate the model with data from HashMap.
      model.setColumnIdentifiers(new String[] {"Table.Column", "No of Lines"});
      for (String key : CoverageDatabaseAttributeMapping.coverageDatabaseAttributeMap.keySet())
      {
          
          model.addRow(new Object[] {key, CoverageDatabaseAttributeMapping.coverageDatabaseAttributeMap.get(key)});
      }
      jTable3.setModel(model);
             
    }//GEN-LAST:event_jButton1MouseClicked
    
    private void jButton2MouseClicked(MouseEvent evt) {
    	JFileChooser fc = new JFileChooser();
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	int returnVal = fc.showOpenDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	//fc.get
            ApplicationPath.setText(fc.getSelectedFile().getAbsolutePath());
        } else {
        }
    }
    //variable declaration
     public static String selectedTable;
     public static String selectedCol;
     public static String fkKey;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JList jList3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel settingsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    private JTextArea sootCommand;
    private JLabel sootCommandLabel;
    private JButton jButton2;
    private JTextField ApplicationPath;
    private JFormattedTextField BrowseForApplication;
}
