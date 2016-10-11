package slcot.mit.edu.a6_dofkalmantracker.log;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import Jama.Matrix;

/**
 * Created by Sebastian on 10/10/2016.
 */
public class MatrixCSVLogger {

    File captureFile;
    PrintWriter printWriter;
    String filename;

    public MatrixCSVLogger(String filename){
        try {
            this.filename = filename;
            captureFile = new File(Environment.getExternalStorageDirectory(), filename);
            printWriter = new PrintWriter(new FileWriter(captureFile, false));
        } catch (IOException ex) {
            Log.e("MatrixCSVLogger", ex.getMessage(), ex);
        }
    }

    public void log(Matrix m, double timestamp){
        if(m.getColumnDimension() != 1)
            throw new RuntimeException("Illegal Matrix Dimensions");

        if(printWriter != null){
            printWriter.print(Double.toString(timestamp) + ",");
            for ( int i=0; i < m.getRowDimension(); i++ )
                printWriter.print(Double.toString(m.get(i, 0)) + ",");
            printWriter.println();
        }
    }

    public String getFilename(){ return filename; }

    public File getFile(){ return captureFile; }

    public void close(){
        if(printWriter != null){
            printWriter.close();
            printWriter = null;
        }
    }
}
