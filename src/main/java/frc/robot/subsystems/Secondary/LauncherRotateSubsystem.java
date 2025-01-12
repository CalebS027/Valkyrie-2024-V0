// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.Secondary;

import com.revrobotics.CANSparkMax;
import com.revrobotics.MotorFeedbackSensor;
//import com.revrobotics.SparkAbsoluteEncoder;
import com.revrobotics.SparkPIDController;
import com.revrobotics.CANSparkLowLevel.MotorType;

//import frc.robot.RobotContainer;
//import frc.robot.Constants;
//import frc.robot.RobotContainer;
import frc.robot.Constants.LauncherConstants;
//import frc.robot.commands.Vision.PVAim;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class LauncherRotateSubsystem extends SubsystemBase {
  public static CANSparkMax m_LauncherRotateMotor;
  public static SparkPIDController m_LauncherRotatePIDController;
  //public static SparkAbsoluteEncoder LauncherRotateEncoder;
  public static DutyCycleEncoder m_LauncherRotateEncoder;
  public static double LauncherRotateSetpoint;
  public static double RotateManualPos;
  
  //public static double RotateManualPos;
  /** Creates a new ArmRotateSubSys. 
 * @param LauncherRotateSubsystem
 * */
  public LauncherRotateSubsystem() {
        // initialize motor
        m_LauncherRotateMotor = new CANSparkMax(LauncherConstants.kLauncherRotate, MotorType.kBrushless);
        DutyCycleEncoder m_LauncherRotateEncoder = new DutyCycleEncoder(2);
        m_LauncherRotateEncoder.setDistancePerRotation(360);
        m_LauncherRotateEncoder.setPositionOffset(72.5);

        
        /**
         * The RestoreFactoryDefaults method can be used to reset the configuration parameters
         * in the SPARK MAX to their factory default state. If no argument is passed, these
         * parameters will not persist between power cycles
         */
        m_LauncherRotateMotor.restoreFactoryDefaults();  //Remove this when we remove the burnFlash() call below
        //LauncherRotateEncoder = m_LauncherRotateMotor.getAbsoluteEncoder(SparkAbsoluteEncoder.Type.kDutyCycle);
        //LauncherRotateEncoder.setPositionConversionFactor(360);
        //LauncherRotateEncoder.setZeroOffset(72.5); //ArmConstants.posOffset);
        //m_armEncoder.setInverted(true);
    
        // initialze PID controller and encoder objects
        m_LauncherRotatePIDController = m_LauncherRotateMotor.getPIDController();
        //m_LauncherRotatePIDController.setFeedbackDevice(LauncherRotateEncoder);
        m_LauncherRotatePIDController.setFeedbackDevice((MotorFeedbackSensor) m_LauncherRotateEncoder);
        m_LauncherRotateMotor.setSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, 85); //ArmConstants.posLowerLimit
        m_LauncherRotateMotor.setSoftLimit(CANSparkMax.SoftLimitDirection.kForward, 241); //ArmConstants.posUpperLimit); 
        m_LauncherRotateMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, true);
        m_LauncherRotateMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true);
        m_LauncherRotateMotor.enableVoltageCompensation(12.0);
        m_LauncherRotateMotor.setSmartCurrentLimit(25);
        m_LauncherRotateMotor.burnFlash();  //Remove this after everything is up and running to save flash wear
    
        // set PID coefficients
        m_LauncherRotatePIDController.setP(0.000066); //ArmConstants.armRotatekP);
        m_LauncherRotatePIDController.setI(0.0); //ArmConstants.armRotatekI);
        m_LauncherRotatePIDController.setD(0.0); //ArmConstants.armRotatekD);
        m_LauncherRotatePIDController.setIZone(0.0); //ArmConstants.armRotatekIz);
        
        // This is an arbitrary feedforward value that is multiplied by the positon of the arm to account
        // for the reduction in force needed to hold the arm vertical instead of hortizontal.  The .abs
        //ensures the value is always positive.  The .cos function uses radians instead of degrees,
        // so the .toRadians converts from degrees to radians.
        m_LauncherRotatePIDController.setFF(.005 * (Math.abs
                                        (Math.cos
                                        ((Math.toRadians(LauncherRotateSetpoint)) -
                                        (Math.toRadians(90))))));
        
        m_LauncherRotatePIDController.setOutputRange(-LauncherConstants.ROTATE_MAX_SPEED, LauncherConstants.ROTATE_MAX_SPEED); //ArmConstants.armRotatekMinOutput, ArmConstants.armRotatekMaxOutput);
    
        /**
         * Smart Motion coefficients are set on a SparkMaxPIDController object
         * 
         * - setSmartMotionMaxVelocity() will limit the velocity in RPM of
         * the pid controller in Smart Motion mode
         * - setSmartMotionMinOutputVelocity() will put a lower bound in
         * RPM of the pid controller in Smart Motion mode
         * - setSmartMotionMaxAccel() will limit the acceleration in RPM^2
         * of the pid controller in Smart Motion mode
         * - setSmartMotionAllowedClosedLoopError() will set the max allowed
         * error for the pid controller in Smart Motion mode
         */
        m_LauncherRotatePIDController.setSmartMotionMaxVelocity(5000.0,0); //ArmConstants.armRotateMaxVel, ArmConstants.armRotateSmartMotionSlot);
        m_LauncherRotatePIDController.setSmartMotionMinOutputVelocity(0.0, 0); //ArmConstants.armRotateMinVel, ArmConstants.armRotateSmartMotionSlot);
        m_LauncherRotatePIDController.setSmartMotionMaxAccel(3000.0,0); //ArmConstants.armRotateMaxAcc, ArmConstants.armRotateSmartMotionSlot);
        m_LauncherRotatePIDController.setSmartMotionAllowedClosedLoopError(0.01, 0); //ArmConstants.armRotateAllowedErr, ArmConstants.armRotateSmartMotionSlot);  
  }

 @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Arm Enc Val", m_LauncherRotateEncoder.getAbsolutePosition());
  }


  
  // public Command rotateAutoPosCommand() {
  //   // implicitly require `this`
  //   return this.runOnce(() -> m_LauncherRotatePIDController.setReference(PVAim.Launcher_Pitch, CANSparkMax.ControlType.kSmartMotion));
  // }
  
  public Command rotatePosCommand(double LauncherRotateSetpoint) {
    // implicitly require `this`
    return this.runOnce(() -> m_LauncherRotatePIDController.setReference(LauncherRotateSetpoint, CANSparkMax.ControlType.kSmartMotion));
  }


  public void setDefaultCommand(){
    //m_armPIDController.setReference(ArmRotateSetpoint, CANSparkMax.ControlType.kSmartMotion);
  }

}
  

