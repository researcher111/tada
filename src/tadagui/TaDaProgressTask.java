package tadagui;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;

import javax.swing.SwingWorker;

public class TaDaProgressTask extends SwingWorker<Integer, Object> implements PropertyChangeListener {

	String status;
	String tableName;
	int number;
	
	
	public int getCurrent(){
		return number;
	}
	
	@Override
	protected Integer doInBackground() throws Exception {
		// TODO Auto-generated method stub
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
            GlobalData.i = GlobalData.i + 10;
            progress =(int) Math.floor(GlobalData.i);
            number = progress;
            setProgress(progress);
            System.out.println(progress);
        }
        }
        catch (InterruptedException ignore) {}
          return 0;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		if ("progress" == evt.getPropertyName() ) {
            setProgress(50);
            }
        }
}
