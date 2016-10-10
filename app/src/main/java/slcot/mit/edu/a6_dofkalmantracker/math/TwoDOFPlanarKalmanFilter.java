package slcot.mit.edu.a6_dofkalmantracker.math;

import Jama.Matrix;

/**
 * Created by Sebastian on 10/10/2016.
 */
public class TwoDOFPlanarKalmanFilter extends KalmanFilter{

    public TwoDOFPlanarKalmanFilter(){
        init(new Matrix(6, 1));
    }

    public TwoDOFPlanarKalmanFilter(Matrix initialConditions){
        init(initialConditions);
    }

    void init(Matrix initialConditions){
        this.future_x_est = new Matrix(6, 1);         // 2 DOF - x,y | 6 values (x,y,dotx,doty,ddotx,ddoty)
        this.x_prev = initialConditions;

        this.future_P_est = new Matrix(6, 6);
        this.P_prev = Matrix.identity(6, 6);       // Initially set to identity for our purposes
        this.Q = new Matrix(6, 6);               // No noise in process
        this.R = Matrix.identity(6, 6).times(2);   // Sensor noise: initial covariance of 2 for our purposes
    }

    public void timeUpdate(double dt){
        // 1.) Update the next state estimate
        double[][] A_prep = {
                {   1,   0,    dt,  0,  0.5*dt*dt,  0           },
                {   0,   1,    0,   dt, 0,          0.5*dt*dt   },
                {   0,   0,    1,   0,  dt,         0           },
                {   0,   0,    0,   1,  0,          dt          },
                {   0,   0,    0,   0,  1,          0           },
                {   0,   0,    0,   0,  0,          1           }
        };
        Matrix A = new Matrix(A_prep);

        this.future_x_est = A.times(this.x_prev);   // Ax + w but we removed w here

        // 2.) Project the covariance ahead
        this.future_P_est = A.times(this.P_prev.times(A.transpose())).plus(this.Q); // A * P_t-1 * A^T + Q
    }

    public void measurementUpdate(Matrix measurement){
        double [][] H_prep = {
                { 0, 0, 0, 0, 1, 0 },
                { 0, 0, 0, 0, 0, 1 }
        };
        Matrix H = new Matrix(H_prep);

        // 1.) Compute the Kalman Gain
        Matrix RHS = H.times(this.future_P_est.times(H.transpose())).plus(this.R);
        Matrix K = this.future_P_est.times(H.transpose().times(RHS.inverse()));

        // 2.) Update the state estimate
        Matrix Z = measurement;
        Matrix predicted_Z = H.times(this.future_x_est);
        this.x_prev = this.future_x_est.plus(K.times(Z.minus(predicted_Z)));

        // 3.) Update the covariance
        Matrix LHS = Matrix.identity(6,6).minus(K.times(H));
        this.P_prev = LHS.times(this.future_P_est);
    }

    public Matrix getState(){
        return this.x_prev;
    }
}
