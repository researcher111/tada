/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tadagui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.ProgressBarUI;

import java.beans.*;
import java.util.Random;

/**
 *
 * @author Lavanya
 */
public class ProgressBar2 extends JPanel implements ActionListener,PropertyChangeListener{
 private JProgressBar progressBar;
    private JButton startButton;
    private JTextArea taskOutput;
    private progressTask task;
     
    
    class progressTask extends SwingWorker<List<Integer>, Object>{

        //@Override
        //protected void process(List<Object> chunks) {
           //taskOutput.append("Task Complete");
        //}
        @Override
        protected List<Integer> doInBackground() throws Exception {
            
        	System.out.println("I am here");

            Random random = new Random();
            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            try {
                Thread.sleep(1000);

            while(progress < 100 && !isCancelled())
            {
            	
                Thread.sleep(random.nextInt(1000));
                GlobalData.i = progressPercent(GlobalData.i);
                progress =(int) Math.floor(GlobalData.i);
                setProgress(progress);
                System.out.println(progress);
            }
            }
            catch (InterruptedException ignore) {}
              return null;
        }

        @Override
        protected void done(){
                System.out.println("lolo");
                //taskOutput.append("Task Complete");
                Toolkit.getDefaultToolkit().beep();
                startButton.setEnabled(true);
                setProgress(0);
        }


        public Double progressPercent( Double j)
        {
            //pass double percentage to this method
            return j+10;
        }
    }
  public ProgressBar2() {
        super(new BorderLayout());

        //Create the demo's UI.
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(5, 5);
        //taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        taskOutput.setBackground(progressBar.getBackground());

        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    }

    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new progressTask();
        task.addPropertyChangeListener(this);
        task.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            taskOutput.setText(String.format(
                    "Completed %d%% of task.\n", task.getProgress()));
        }
    }


    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Anonymizing Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ProgressBar2();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
