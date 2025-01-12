// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants.AprilTagConstants;
import frc.robot.subsystems.Secondary.Climber;
import frc.robot.subsystems.Secondary.LEDs;
import frc.robot.subsystems.Secondary.LauncherRotateSubsystem;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.photonvision.PhotonCamera;
import swervelib.parser.SwerveParser;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to each mode, as
 * described in the TimedRobot documentation. If you change the name of this class or the package after creating this
 * project, you must also update the build.gradle file in the project.
 */
public class Robot extends TimedRobot
{

  private static Robot   instance;
  private        Command m_autonomousCommand;

  private RobotContainer m_robotContainer;

  private Timer disabledTimer;
  
  public static PhotonCamera camObj = new PhotonCamera("camObj");
  public static PhotonCamera camAprTgLow = new PhotonCamera("camAprTgLow");
  public static PhotonCamera camAprTgHigh = new PhotonCamera("camAprTgHigh");
   
  public static DigitalInput sensorIntake = new DigitalInput(0); //This is the lower sensor, it will be true when a note is first intaked
  public static DigitalInput sensorOuttake = new DigitalInput(1); //This is the upper sensor, it will be true when a note is ready for outtake

  public Robot()
  {
    instance = this;
  }

  public static Robot getInstance()
  {
    return instance;
  }

  /**
   * This function is run when the robot is first started up and should be used for any initialization code.
   */
  @Override
  public void robotInit()
  {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();

    // Create a timer to disable motor brake a few seconds after disable.  This will let the robot stop
    // immediately when disabled, but then also let it be pushed more 
    disabledTimer = new Timer();
    camObj.setDriverMode(false);
    camAprTgHigh.setDriverMode(false);
    camAprTgLow.setDriverMode(false);
    DriverStation.silenceJoystickConnectionWarning(true); // Disable joystick connection warning
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics that you want ran
   * during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic()
  {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
  }

  /**
   * This function is called once each time the robot enters Disabled mode.
   */
  @Override
  public void disabledInit()
  {
    m_robotContainer.setMotorBrake(true);
    disabledTimer.reset();
    disabledTimer.start();
    RobotContainer.driverXbox.setRumble(RumbleType.kBothRumble, 0);
    LEDs.setLED(.99);
  }

  @Override
  public void disabledPeriodic()
  {
    if (disabledTimer.hasElapsed(Constants.Drivebase.WHEEL_LOCK_TIME))
    {
      m_robotContainer.setMotorBrake(false);
      disabledTimer.stop();
    }
  }

  /**
   * This autonomous runs the autonomous command selected by your {@link RobotContainer} class.
   */
  @Override
  public void autonomousInit()
  {
    m_robotContainer.setMotorBrake(true);
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    aprilTagAlliance();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null)
    {
      m_autonomousCommand.schedule();
    }
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic()
  {
  }

  @Override
  public void teleopInit()
  {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    
    if (m_autonomousCommand != null)
    {
      m_autonomousCommand.cancel();
    }

    m_robotContainer.setDriveMode();
    aprilTagAlliance();
    RobotContainer.driverXbox.setRumble(RumbleType.kBothRumble, 0);
    //m_robotContainer.setMotorBrake(true);
    LauncherRotateSubsystem.LauncherRotateSetpoint = 90;

  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic()
  {
    m_robotContainer.spencerButtons();
    watchForNote();

    if (RobotContainer.engineerXbox.getRawButtonPressed(2)) {
      Climber.m_climberPIDController.setGoal(5);
    } else if (RobotContainer.engineerXbox.getRawButtonPressed(3)) {
      Climber.m_climberPIDController.setGoal(0);
    }
    Climber.m_climberMotor1.setVoltage(
      Climber.m_climberPIDController.calculate(Climber.ClimberEncoder.getDistance())
          + Climber.m_climberFF.calculate(Climber.m_climberPIDController.getSetpoint().velocity));
}
  

  @Override
  public void testInit()
  {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
    try
    {
      new SwerveParser(new File(Filesystem.getDeployDirectory(), "swerve"));
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic()
  {
  }

  /**
   * This function is called once when the robot is first started up.
   */
  @Override
  public void simulationInit()
  {
  }

  /**
   * This function is called periodically whilst in simulation.
   */
  @Override
  public void simulationPeriodic()
  {
  }

  public static void aprilTagAlliance(){
    
    Optional<Alliance> allianceColor = DriverStation.getAlliance();
    if (allianceColor.isPresent()) {
      if (allianceColor.get() == Alliance.Red) {
        AprilTagConstants.ampID     = 5;
        AprilTagConstants.speakerID = 4;
        AprilTagConstants.stageIDA  = 13;
        AprilTagConstants.stageIDB  = 12;
        AprilTagConstants.stageIDC  = 11;
      }
      if (allianceColor.get() == Alliance.Blue) {
        AprilTagConstants.ampID     = 6;
        AprilTagConstants.speakerID = 7;
        AprilTagConstants.stageIDA  = 14;
        AprilTagConstants.stageIDB  = 15;
        AprilTagConstants.stageIDC  = 16; //change to 16 when Matt figures out his purpose in tags....
      }
     }
  }

  public static boolean watchForNote(){
    boolean hasTargets = false;
    if (sensorIntake.get() == false && sensorOuttake.get() == false){
      var result = camObj.getLatestResult(); //Get the latest result from PhotonVision
      hasTargets = result.hasTargets(); // Check if the latest result has any targets.
      if (hasTargets == true){
        System.out.println("Note Found - Press and hold B to retrieve the note!");
        LEDs.setLEDwBlink(.65, .125);
        //RobotContainer.pulseRumble();
      } else{
        //RobotContainer.driverXbox.setRumble(RumbleType.kBothRumble, 0);
        LEDs.setLED(.99);
      }
    }
    return hasTargets;
  }
  
}
