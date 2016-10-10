package slcot.mit.edu.a6_dofkalmantracker.math;

import Jama.Matrix;

/**
 * Created by Sebastian on 10/10/2016.
 */
public abstract class KalmanFilter {

    Matrix future_P_est  = null;       // estimate of next covariance matrix
    Matrix P_prev = null;       // prior knowledge of state (covariance matrix)
    Matrix Q = null;        // process noise matrix (normally distributed) with covariance matrix Q
    Matrix R = null;        // measurement noise matrix (normally distributed) with covariance matrix R

    Matrix future_x_est = null;        // current state
    Matrix x_prev = null;              // previous state

    public KalmanFilter(){

    }

    public abstract void timeUpdate(double dt);

    public abstract void measurementUpdate(Matrix measurement);

    public abstract Matrix getState();
}
