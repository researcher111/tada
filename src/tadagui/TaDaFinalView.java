/*
 * TaDaFinalView.java
 */

package tadagui;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;

import soot.toolkits.scalar.InitAnalysis;
import tadadriver.CoverageDatabaseAttributeMapping;
import tadadriver.TaDaMain;
import tadagui.ProgressBar.progressTask;

import database.anonymization.DatabaseAnonymizer;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.ProgressMonitor;

import java.io.File;
import java.io.IOException;
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
public class TaDaFinalView extends FrameView {

    public TaDaFinalView(SingleFrameApplication app) {
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
            JFrame mainFrame = TaDaFinalApp.getApplication().getMainFrame();
            aboutBox = new TaDaFinalAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        TaDaFinalApp.getApplication().show(aboutBox);
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList3 = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        jLabel6 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        sootCommand = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
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

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(TaDaFinalApp.class).getContext().getResourceMap(TaDaFinalView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jList2.setName("jList2"); // NOI18N
        jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList2ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList2);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jList3.setName("jList3"); // NOI18N
        jList3.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList3ValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jList3);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        {
        	computeCoverage = new JButton();
        	computeCoverage.setName("computeCoverage");
        	computeCoverage.addMouseListener(new MouseAdapter() {
        		public void mouseClicked(MouseEvent evt) {
        			computeCoverageImpactClicked(evt);
        		}
        	});
        }
        {
        	permutationProbability = new JTextField();
        	permutationProbability.setName("permutationProbability");
        }
        {
        	jButton3 = new JButton();
        	jButton3.setName("jButton3");
        	jButton3.addMouseListener(new MouseAdapter() {
        		public void mouseClicked(MouseEvent evt) {
        			jButton3MouseClicked(evt);
        		}
        	});
        }

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        {
        	Anonymize = new JButton();
        	Anonymize.setName("Anonymize");
        }

        jTextField1.setText(resourceMap.getString("Application Path.text")); // NOI18N
        jTextField1.setName("Application Path"); // NOI18N

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jScrollPane10.setName("jScrollPane10"); // NOI18N
        {
        	jList5 = new javax.swing.JList();
        	jScrollPane10.setViewportView(jList5);
        	jList5.setName("jList5"); // NOI18N
        	jList5.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
        		public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        			qiListValueChanged(evt);
        		}
        	});
        }
        
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addQIMouseClicked(evt);
            }
        });

        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setName("jButton7"); // NOI18N
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                removeQIMouseClicked(evt);
            }
        });

        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setName("jButton8"); // NOI18N
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createSequentialGroup()
        	.addContainerGap()
        	.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	    .addComponent(jLabel6, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jLabel1, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jLabel2, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jLabel3, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
        	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 377, GroupLayout.PREFERRED_SIZE)
        	        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
        	    .addComponent(jScrollPane10, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 382, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jScrollPane3, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 382, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 382, GroupLayout.PREFERRED_SIZE)
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addGap(37)
        	        .addComponent(computeCoverage, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
        	        .addComponent(jButton6, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addGap(24)
        	        .addComponent(jButton7, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addGap(34)
        	        .addComponent(jButton8, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addGap(193)))
        	.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	    .addComponent(jButton2, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jTextField1, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(permutationProbability, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jButton3, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
        	.addGap(19)
        	.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	    .addComponent(jButton1, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(Anonymize, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
        	.addContainerGap(79, 79));
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createSequentialGroup()
        	.addContainerGap()
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE)
        	        .addGap(18)
        	        .addGroup(jPanel1Layout.createParallelGroup()
        	            .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
        	            .addGroup(jPanel1Layout.createSequentialGroup()
        	                .addGap(43)
        	                .addGroup(jPanel1Layout.createParallelGroup()
        	                    .addComponent(jButton1, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	                    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	                        .addGap(22)
        	                        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	                        .addGap(45)))
        	                .addGap(13))))
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addGap(60)
        	        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addGap(63)
        	        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
        	        .addGap(51)))
        	.addGap(18)
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
        	        .addGap(105))
        	    .addComponent(jScrollPane3, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE)
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addGap(31)
        	        .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
        	        .addGap(48)))
        	.addGap(31)
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addComponent(jButton6, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jButton7, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
        	    .addComponent(jButton8, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE))
        	.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
        	.addGroup(jPanel1Layout.createParallelGroup()
        	    .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	        .addGroup(jPanel1Layout.createParallelGroup()
        	            .addComponent(jScrollPane10, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE)
        	            .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	                .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
        	                .addGap(34)))
        	        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	        .addComponent(computeCoverage, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
        	        .addGap(0, 0, Short.MAX_VALUE))
        	    .addGroup(jPanel1Layout.createSequentialGroup()
        	        .addPreferredGap(jScrollPane10, permutationProbability, LayoutStyle.ComponentPlacement.INDENT)
        	        .addGroup(jPanel1Layout.createParallelGroup()
        	            .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	                .addComponent(permutationProbability, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
        	                .addGap(18)
        	                .addComponent(jButton3, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
        	                .addGap(0, 0, Short.MAX_VALUE))
        	            .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
        	                .addGap(17)
        	                .addComponent(Anonymize, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
        	                .addGap(0, 92, Short.MAX_VALUE)))
        	        .addGap(106)))
        	.addContainerGap(192, 192));
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton8MouseClicked(evt);
            }
        });
        Anonymize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AnonymizeClicked(evt);
            }

			
        });

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

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
        jScrollPane5.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 672, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(445, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(205, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane6.setName("jScrollPane6"); // NOI18N

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
        jScrollPane6.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 604, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(502, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(251, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane7.setName("jScrollPane7"); // NOI18N

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
        jScrollPane7.setViewportView(jTable3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(548, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(264, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

       

        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPane9.setName("jScrollPane9"); // NOI18N

        sootCommand.setColumns(20);
        sootCommand.setLineWrap(true);
        sootCommand.setRows(5);
        sootCommand.setText(resourceMap.getString("SootCommand.text")); // NOI18N

        String javalibloc = "/usr/lib/jvm/java-6-openjdk-amd64/jre/lib";
        String javacommonloc = "/usr/lib/jvm/java-6-openjdk-common/jre/lib";
        
        //unix usage
    	//sootCommand.setText("--interactive-mode  --d  \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\sootOutput\" --cp \"/C:/Program\\ Files/Java/jre6/lib/jce.jar;/C:/Program\\ Files/Java/jre6/lib/charsets.jar;/C:/Program\\ Files/Java/jre6/lib/ext/dnsns.jar;/C:/Program\\ Files/Java/jre6/lib/jsse.jar;/C:/Sample/src;/C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;/C:/Program\\ Files/Java/jre6/lib/resources.jar;/C:/Documents\\ and\\ Settings/kunal_taneja/workspace/Sample/bin/;/C:/Program\\ Files/Java/jre6/lib/ext/localedata.jar;/C:/Program\\ Files/Java/jre6/lib/rt.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;;C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\src;C:\\Program\\ Files\\Java\\jre6\\lib\\resources.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\rt.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jsse.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jce.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\charsets.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/DurboDax/src;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/UnixUsage/src;C:/Documents\\ and\\ Settings/kunal_taneja/GlassFish_v3_Prelude/glassfish/modules/javax.servlet.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\UnixUsage\\src\" app.CourseInfo");
    	//RiskInsurance
    	//sootCommand.setText("--interactive-mode  --d  \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\sootOutput\" --cp \"/C:/Program\\ Files/Java/jre6/lib/jce.jar;/C:/Program\\ Files/Java/jre6/lib/charsets.jar;/C:/Program\\ Files/Java/jre6/lib/ext/dnsns.jar;/C:/Program\\ Files/Java/jre6/lib/jsse.jar;/C:/Sample/src;/C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;/C:/Program\\ Files/Java/jre6/lib/resources.jar;/C:/Documents\\ and\\ Settings/kunal_taneja/workspace/Sample/bin/;/C:/Program\\ Files/Java/jre6/lib/ext/localedata.jar;/C:/Program\\ Files/Java/jre6/lib/rt.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;;C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\Sample\\src;C:\\Program\\ Files\\Java\\jre6\\lib\\resources.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\rt.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jsse.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jce.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\charsets.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/DurboDax/src;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/UnixUsage/src;C:/Documents\\ and\\ Settings/kunal_taneja/GlassFish_v3_Prelude/glassfish/modules/javax.servlet.jar;C:/Documents\\ and\\ Settings/kunal_taneja/workspace/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\RiskInsurance\\src\" com.riskIt.app.MainClass");
        //sootCommand.setText("--interactive-mode  --d  \"C:\\sootOutput\" --cp \"C:/Program\\ Files/Java/jre6/lib/jce.jar;/C:/Program\\ Files/Java/jre6/lib/charsets.jar;/C:/Program\\ Files/Java/jre6/lib/ext/dnsns.jar;/C:/Program\\ Files/Java/jre6/lib/jsse.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;/C:/Program\\ Files/Java/jre6/lib/resources.jar;/C:/Program\\ Files/Java/jre6/lib/ext/localedata.jar;/C:/Program\\ Files/Java/jre6/lib/rt.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;/C:/Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\resources.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\rt.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jsse.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\jce.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\charsets.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\Program\\ Files\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\Program\\ Files/Java/jre6/lib/ext/sunjce_provider.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunmscapi.jar;C:/Program\\ Files/Java/jre6/lib/ext/sunpkcs11.jar;C:/Users/kunal/workspace/DurboDax/src;C:/Users/kunal/workspace/UnixUsage/src;C:/Program Files/glassfish-3.0.1/glassfish/modules/javax.servlet.jar;C:/Users/kunal/workspace/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:/Users/kunal/workspace\\RiskInsurance\\src\" com.riskIt.app.MainClass");
        //sootCommand.setText("--interactive-mode  --d  \"C:\\sootOutput\" --cp \"C:/DevTools/Java/jre6/lib/jce.jar;C:/DevTools/Java/jre6/lib/charsets.jar;C:/DevTools/Java/jre6/lib/ext/dnsns.jar;C:/DevTools/Java/jre6/lib/jsse.jar;C:/DevTools/Java/jre6/lib/ext/sunpkcs11.jar;C:/DevTools/Java/jre6/lib/resources.jar;C:/DevTools/Java/jre6/lib/ext/localedata.jar;C:/DevTools/Java/jre6/lib/rt.jar;C:/DevTools/Java/jre6/lib/ext/sunmscapi.jar;C:/DevTools/Java/jre6/lib/ext/sunjce_provider.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\resources.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\rt.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\jsse.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\jce.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\charsets.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\PROGRA~1/Java/jre6/lib/ext/sunjce_provider.jar;C:/DevTools/Java/jre6/lib/ext/sunmscapi.jar;C:/DevTools/Java/jre6/lib/ext/sunpkcs11.jar;C:/Users/kunal/workspace/DurboDax/src;C:/Users/kunal/workspace/UnixUsage/src;C:/Program Files/glassfish-3.0.1/glassfish/modules/javax.servlet.jar;C:/Users/kunal/workspace/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:/Users/kunal/workspace/DurboDax/src/\" durbodax.Main");
        //C:\\Documents\\ and\\ Settings\\kunal_taneja\\workspace\\UnixUsage\\src\" app.CourseInfo
        //sootCommand.setText("--interactive-mode  --d  \"C:\\sootOutput\" --cp \"C:/DevTools/Java/jre6/lib/jce.jar;C:/DevTools/Java/jre6/lib/charsets.jar;C:/DevTools/Java/jre6/lib/ext/dnsns.jar;C:/DevTools/Java/jre6/lib/jsse.jar;C:/DevTools/Java/jre6/lib/ext/sunpkcs11.jar;C:/DevTools/Java/jre6/lib/resources.jar;C:/DevTools/Java/jre6/lib/ext/localedata.jar;C:/DevTools/Java/jre6/lib/rt.jar;C:/DevTools/Java/jre6/lib/ext/sunmscapi.jar;C:/DevTools/Java/jre6/lib/ext/sunjce_provider.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\resources.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\rt.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\jsse.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\jce.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\charsets.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\PROGRA~1/Java/jre6/lib/ext/sunjce_provider.jar;C:/DevTools/Java/jre6/lib/ext/sunmscapi.jar;C:/DevTools/Java/jre6/lib/ext/sunpkcs11.jar;C:/Users/kunal/workspace/DurboDax/src;C:/Users/kunal/workspace/UnixUsage/src;C:/Program Files/glassfish-3.0.1/glassfish/modules/javax.servlet.jar;C:/Workspace/PRIest/RiskInsurance/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:/Workspace/PRIest/RiskInsurance/src/\" com.riskIt.app.MainClass");
        //sootCommand.setText("--interactive-mode  --d  \"C:\\sootOutput\" --cp \"C:/DevTools/Java/jre6/lib/jce.jar;C:/DevTools/Java/jre6/lib/charsets.jar;C:/DevTools/Java/jre6/lib/ext/dnsns.jar;C:/DevTools/Java/jre6/lib/jsse.jar;C:/DevTools/Java/jre6/lib/ext/sunpkcs11.jar;C:/DevTools/Java/jre6/lib/resources.jar;C:/DevTools/Java/jre6/lib/ext/localedata.jar;C:/DevTools/Java/jre6/lib/rt.jar;C:/DevTools/Java/jre6/lib/ext/sunmscapi.jar;C:/DevTools/Java/jre6/lib/ext/sunjce_provider.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\resources.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\rt.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\jsse.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\jce.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\charsets.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\ext\\dnsns.jar;C:\\PROGRA~1\\Java\\jre6\\lib\\ext\\localedata.jar;C:\\PROGRA~1/Java/jre6/lib/ext/sunjce_provider.jar;C:/DevTools/Java/jre6/lib/ext/sunmscapi.jar;C:/DevTools/Java/jre6/lib/ext/sunpkcs11.jar;C:/Users/kunal/workspace/DurboDax/src;C:/Users/kunal/workspace/UnixUsage/src;C:/Program Files/glassfish-3.0.1/glassfish/modules/javax.servlet.jar;C:/Workspace/PRIest/DurboDax/src/\"  -p jb preserve-source-annotations -output-format J --keep-line-number --xml-attributes --src-prec java  -process-dir \"C:/Workspace/PRIest/DurboDax/src/\" durbodax.Main");

    	String runcmd = "--interactive-mode --d \"/home/cmc/projects/priest/workspace/sootOutput/\" " +
			"--cp \"" +
			javacommonloc+"/jce.jar:" +
			javacommonloc+"/charsets.jar:" +
			javacommonloc+"/ext/dnsns.jar:" +
			javacommonloc+"/jsse.jar:" +
			javacommonloc+"/ext/sunpkcs11.jar:" +
			javacommonloc+"/resources.jar:" +
			"/home/cmc/projects/priest/workspace/Sample/bin:" +
			javacommonloc+"/ext/localedata.jar:" +
			javalibloc+"/rt.jar:" +
			javacommonloc+"/ext/sunmscapi.jar:" + // not found on nanook
			javacommonloc+"/ext/sunjce_provider.jar:" +
			"/home/cmc/projects/preist/workspace/Sample/src:" +
			/*javalibloc+"/ext/resources.jar:" +
			javalibloc+"/rt.jar:" + // why so many repeats?
			javalibloc+"/jsse.jar:" +
			javalibloc+"/jce.jar:" +
			javalibloc+"/charsets.jar:" +
			javalibloc+"/ext/dnsns.jar:" +
			javalibloc+"/ext/localedata.jar:" +
			javalibloc+"/ext/sunjce_provider.jar:" +
			javalibloc+"/ext/sunmscapi.jar:" +
			javalibloc+"/ext/sunpkcs11.jar:" +*/
			"/home/cmc/projects/priest/workspace/sootOutput:" +
			"/home/cmc/projects/priest/subjectapps/DurboDax/src:" +
			"/home/cmc/projects/priest/subjectapps/UnixUsage/src:" +
			//"C:/Documents\\ and\\ Settings/kunal_taneja/GlassFish_v3_Prelude/glassfish/modules/javax.servlet.jar:" +
			"//home/cmc/projects/subjectapps/RiskInsurance/src\" " +
			"-p jb preserve-source-annotations " +
			"-output-format J " +
			"--keep-line-number " +
			"--xml-attributes " +
			"--src-prec java " +

		// SET FOR DurboDax
			"-process-dir \"/home/cmc/projects/priest/subjectapps/DurboDax/src\" " +
			"durbodax.Main";

    	// SET FOR RiskInsurance
			//"-process-dir \"/home/cmc/projects/priest/subjectapps/RiskInsurance/src\" " +
			//"com.riskIt.app.MainClass";

    	//System.out.println(runcmd);

    	sootCommand.setText(runcmd);
        
        sootCommand.setName("SootCommand"); // NOI18N
        jScrollPane9.setViewportView(sootCommand);

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jLabel5)
                .addGap(26, 26, 26)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(483, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(148, 148, 148)
                        .addComponent(jLabel5)))
                .addContainerGap(220, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        jPanel7.setName("jPanel7"); // NOI18N

        jScrollPane11.setName("jScrollPane11"); // NOI18N

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
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
        jTable5.setName("jTable5"); // NOI18N
        jScrollPane11.setViewportView(jTable5);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1145, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 696, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(409, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 552, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(185, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        mainPanel.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 1150, 580));

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(TaDaFinalApp.class).getContext().getActionMap(TaDaFinalView.class, this);
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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1184, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1014, Short.MAX_VALUE)
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


    //Variable Declaration
     public static String selectedTable;
     public static String selectedCol;
     public static String fkKey;
     static ArrayList arSense = new ArrayList();
     public static ArrayList arQI = new ArrayList();
     static String[] qiValue;
     static String[] listValue;
     
    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        // TODO add your handling code here:
        selectedTable = (String)jList1.getSelectedValue();
        //System.out.println("You have selected this table "+selectedTable);
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

    private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList2ValueChanged
        // TODO add your handling code here:
        selectedCol = (String)jList2.getSelectedValue();
        //System.out.println("You have selected this table "+selectedCol);
        //ArrayList colNames = AbstractComboBoxModel.processTableValue();
        jList3.setModel(new javax.swing.AbstractListModel() {
            String[] strings = AbstractComboBoxModel.processAttributeValue();
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });

       
}//GEN-LAST:event_jList2ValueChanged

    private void jList3ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList3ValueChanged
        // TODO add your handling code here:
        fkKey = (String)jList3.getSelectedValue();
        //System.out.println("You have selected this table "+fkKey);
        AbstractComboBoxModel.getForeignValues();
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
                AbstractComboBoxModel.foreignData, AbstractComboBoxModel.foreignColumnNames
                ));
    }//GEN-LAST:event_jList3ValueChanged
    private boolean applicationAnalyzed = false;
    
    
    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
         // TODO add your handling code here:
    	
    	ArrayList<String> arguments = new ArrayList<String>();
    	String txt = sootCommand.getText();
    	txt = sootCommand.getText().replace("\n", "");
    	 
    	String[] args = txt.split("\"");
    	for (String string : args) {
			if (string.split("[^\\\\][\\s]").length == 1){
				string = string.replace("\\ ", " ");
				arguments.add(string.trim());
			}
			else
				if(string.trim().startsWith("-")){
					for(String s : string.split("[\\s]"))
						if(s.trim().length() >0 )
							arguments.add(s.trim());
				}
				else
					arguments.add(string);
		}
    	String[] str = new String[arguments.size()];
    	int i=0;
    	for (String string : arguments) {
			str[i++] = string;
		}

		initializeProgressBar("Initializing Soot..");
		CoverageInfo coverage = new CoverageInfo(str);
		Thread computeCoverageInfo = new Thread(coverage, "fillTable");
		computeCoverageInfo.start();
		

    }//GEN-LAST:event_jButton1MouseClicked
    class CoverageInfo implements Runnable{
	  	public Thread waitFor;
	  	String[] args;
	  	
	  	public CoverageInfo(String[] str) {
	  		args = str;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			 TaDaMain tada = new TaDaMain();
		     tada.mainArguments = args;
		     Thread thread = new Thread(tada, "soot");
		     thread.run();
			
			 DefaultTableModel model = new DefaultTableModel();
		      // Populate the model with data from HashMap.
		      model.setColumnIdentifiers(new String[] {"Table.Column", "No of Lines", "percentage"});
		      TreeMap<Integer, HashSet<String>> sortedMap = CoverageDatabaseAttributeMapping.getSortedMap();
		      int total =0;
		      for (Integer key : sortedMap.keySet())
		    	  total = total + key;
		      for (Integer key : sortedMap.keySet()){
		    	  HashSet<String> attributes = sortedMap.get(key);
		    	  for (String attr : attributes) {
		    		  int percent = key*100/total;
		    		  model.addRow(new Object[] {attr, key, percent});
		    	  }
		      }
		      model.addRow(new Object[] {"Total", total});
		      jTable3.setModel(model);
		      applicationAnalyzed = true;
		}
	  
  }
    
    private void jList4ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList4ValueChanged
        // TODO add your handling code here:
    	
        
    }//GEN-LAST:event_jList4ValueChanged

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser();
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	int returnVal = fc.showOpenDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	//fc.get
            jTextField1.setText(fc.getSelectedFile().getAbsolutePath());
        } else {
        }
    }//GEN-LAST:event_jButton2MouseClicked
    
    double probability = 1.0;
    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        // TODO add your handling code here:
       String text = permutationProbability.getText();
       try{
    	   probability = Double.parseDouble(text);
    	   if(probability < 0.0 || probability > 1.0)
    		   throw new Exception("Illegal probabilty value");
       }
       catch(Exception e){
    	   displayErrorMessage("Illegal Probability Value : " + text);
       }

    }//GEN-LAST:event_jButton3MouseClicked

    static JDialog errorInfo;
    public static void displayErrorMessage(String message){
    	JFrame mainFrame = TaDaFinalApp.getApplication().getMainFrame();
    	errorInfo = new ErrorBox(mainFrame	,message);
    	errorInfo.setLocationRelativeTo(mainFrame);
    	errorInfo.setTitle("Estimated Coverage Loss");
        TaDaFinalApp.getApplication().show(errorInfo);
    }
    
    static JDialog anonymizationResultDialog;
    public static void displayAnonymizationResults(double totalRecords, double anonymizationScore, double timeTaken, double numberOfUniqueRecords,
    		double uniqueNessScore){
    	JFrame mainFrame = TaDaFinalApp.getApplication().getMainFrame();    	
    	anonymizationResultDialog = new AnonymizationResultBox(mainFrame, totalRecords, anonymizationScore, timeTaken, numberOfUniqueRecords,
    			uniqueNessScore);
    	anonymizationResultDialog.setLocationRelativeTo(mainFrame);
    	anonymizationResultDialog.setTitle("Anonymization Results");
    	TaDaFinalApp.getApplication().show(anonymizationResultDialog);
    }
    
    
    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        // TODO add your handling code here:
         //Populating the QI Sensitive Values

        String sensitiveValue ;
        sensitiveValue = selectedTable.concat(".").concat(selectedCol);
        arSense.add(sensitiveValue);
        listValue = new String[arSense.size()];
        for (int i = 0; i < arSense.size(); i++)
        {
             listValue[i] = (String)arSense.get(i);
        }
        jList5.setModel(new javax.swing.AbstractListModel() {
            String[] strings = listValue;
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
            public String[] getElements(){ return strings;}
        });
    }//GEN-LAST:event_jButton5MouseClicked
    public static String[] temp;
    public static String sensitiveTable;
    private JButton Anonymize;
    public static String sensitiveCol;

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        // TODO add your handling code here:
        String s=(String) jList5.getSelectedValue();
        System.out.print(s);
        String[] temp=AbstractComboBoxModel.splitFunc(s);
        String table = temp[0].toString();
        String col = temp[1].toString();
        
        AbstractComboBoxModel.getSensitiveValues(table,col);
        

    }//GEN-LAST:event_jButton4MouseClicked

    private void qiListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList5ValueChanged
        // TODO add your handling code here:
    	@SuppressWarnings("unused")
		int i=0;
    }//GEN-LAST:event_jList5ValueChanged

    private void addQIMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
        // TODO add your handling code here:
        //Function to add to QI elist
        String qiString ;
        if(selectedTable == null || selectedCol == null)
        	return;
        qiString = selectedTable.concat(".").concat(selectedCol);
        qiString = qiString.toLowerCase();
        arQI.add(qiString);
        qiValue = new String[arQI.size()];
        for (int i = 0; i < arQI.size(); i++)
        {
             qiValue[i] = (String)arQI.get(i);
        }
        jList5.setModel(new javax.swing.AbstractListModel() {
            String[] strings = qiValue;
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });

      
    }//GEN-LAST:event_jButton6MouseClicked

    private void removeQIMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton7MouseClicked
        // TODO add your handling code here:
        //To remove from QI list
        Object[] removeVal = jList5.getSelectedValues();
        for(Object obj:removeVal)
        {
            System.out.print("Remove vals:\n"+obj);
            arQI.remove(obj);
        }

              qiValue = new String[arQI.size()];
              for (int i = 0; i < arQI.size(); i++)
                {
                    qiValue[i] = (String)arQI.get(i);
                }
            jList5.setModel(new javax.swing.AbstractListModel() {
            String[] strings = qiValue;
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
    }//GEN-LAST:event_jButton7MouseClicked

    private void computeCoverageImpactClicked(MouseEvent evt) {
		// TODO Auto-generated method stub
		if(!applicationAnalyzed){
			
	            JFrame mainFrame = TaDaFinalApp.getApplication().getMainFrame();
	            coverageInfo = new ErrorBox(mainFrame, "Analyze an application first");
	            coverageInfo.setLocationRelativeTo(mainFrame);
	            coverageInfo.setTitle("Estimated Coverage Loss");
	        
	        TaDaFinalApp.getApplication().show(coverageInfo);
		}
		else{
			
	            JFrame mainFrame = TaDaFinalApp.getApplication().getMainFrame();
	            HashSet<String> qiSet = new HashSet<String>(arQI);
	            if(qiSet.size() > 0){
	            	int loc = (Integer) CoverageDatabaseAttributeMapping.computeEstimatedCoverageLoss(qiSet).get(0);
		            double percent = (Double) CoverageDatabaseAttributeMapping.computeEstimatedCoverageLoss(qiSet).get(1);
		            coverageInfo = new CoverageInfoBox(mainFrame, loc, (int)percent);
		            coverageInfo.setLocationRelativeTo(mainFrame);
		            coverageInfo.setTitle("Estimated Coverage Loss");
	            }
	            else{
	            	coverageInfo = new ErrorBox(mainFrame, "No QIs Selected");
		            coverageInfo.setLocationRelativeTo(mainFrame);
		            coverageInfo.setTitle("Estimated Coverage Loss");
	            }
	            	
	        TaDaFinalApp.getApplication().show(coverageInfo);
		}
	}
    
    public static String qiTable;
    public static String qiCol;
    private void jButton8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton8MouseClicked
        // TODO add your handling code here:
        //To view the result of QI list
         String s=(String) jList5.getSelectedValue();
        System.out.print(s);
        String[] temp=AbstractComboBoxModel.splitFunc(s);
        String table = temp[0].toString();
        String col = temp[1].toString();

        AbstractComboBoxModel.getQIValues(table, col);
        jTable5.setModel(new javax.swing.table.DefaultTableModel(
                AbstractComboBoxModel.qiData, AbstractComboBoxModel.qiColumnNames
                ));
      
        
    }//GEN-LAST:event_jButton8MouseClicked

    public void AnonymizeClicked(MouseEvent evt) {
    	int size =arQI.size();
    	DatabaseAnonymizer dbAnonymizer = null;
    	initializeProgressBar("Anonymizing Database");
    	dbAnonymizer = new DatabaseAnonymizer(probability);
    	/*
		try {
			dbAnonymizer = new DatabaseAnonymizer(probability, "c:\\script");
			dbAnonymizer = new DatabaseAnonymizer(probability);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    	for(int i=0; i<size; i++){
    		String tableColumn = (String) arQI.get(i);
    		int sep=tableColumn.indexOf('.');
    		String table = tableColumn.substring(0, sep);
    		String column = tableColumn.substring(sep+1, tableColumn.length());
    		dbAnonymizer.addQI(table.toLowerCase(), column.toLowerCase());
    	}
		
		//dbAnonymizer.permuteDataInAllTables();
    	Thread anonymizationThread = new Thread(dbAnonymizer, "anonymization");
    	anonymizationThread.start();
	}

	private void initializeProgressBar(String status) {
		//ProgressBar.main(null);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setVisible(true);
		progressBar.setString(status);
		progressBar.setIndeterminate(true);
		//progressBar.setStringPainted(true);
		progressBar.setSize(500, 10);
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JList jList3;
    private javax.swing.JList jList5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable5;
    private javax.swing.JTextArea sootCommand;
    private JButton jButton3;
    private JTextField permutationProbability;
    private JButton computeCoverage;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    public static javax.swing.JProgressBar progressBar;
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
    private JDialog coverageInfo;
}
