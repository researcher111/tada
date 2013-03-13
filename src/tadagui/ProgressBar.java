/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tadagui;

/**
 *
 * @author Lavanya
 */
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import java.beans.*;
import java.util.Random;



public class ProgressBar extends JPanel implements PropertyChangeListener, ActionListener
{
    
    private ProgressMonitor progressMonitor;
    private JButton startButton;
    private JTextArea taskOutput;
    private  progressTask task;
   
    public ProgressBar() {
        super(new BorderLayout());

        //Create the demo's UI.
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        add(startButton, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    }

    public void actionPerformed(ActionEvent e) {
         progressMonitor = new ProgressMonitor(ProgressBar.this,
                                  "Running a Long Task",
                                  "", 0, 100);
        progressMonitor.setProgress(0);
        task = new progressTask();
        task.addPropertyChangeListener(this);
        task.execute();
        startButton.setEnabled(false);
    }




    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ProgressMonitorDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ProgressBar();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

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
                progressMonitor.setProgress(0);
        }


        public Double progressPercent( Double j)
        {
            //pass double percentage to this method
            return j+10;
        }
    } 
    
     /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message =
                String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            taskOutput.append(message);
            if (progressMonitor.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                    taskOutput.append("Task canceled.\n");
                } else {
                    taskOutput.append("Task completed.\n");
                }
                startButton.setEnabled(true);
            }
        }

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

    







