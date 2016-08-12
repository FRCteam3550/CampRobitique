
package org.usfirst.frc.team3550.robot;


import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.RobotDrive;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.vision.AxisCamera;

/**
 * This is a demo program showing the use of the RobotDrive class.
 * The SampleRobot class is the base of a robot application that will automatically call your
 * Autonomous and OperatorControl methods at the right time as controlled by the switches on
 * the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're inexperienced,
 * don't. Unless you know what you are doing, complex code will be much more difficult under
 * this system. Use IterativeRobot or Command-Based instead if you're new.
 */
public class Robot extends SampleRobot {
	
	//Declaration de toutes les composantes
	
	//Systeme de deplacement
    RobotDrive myRobot; //Declaration du systeme de deplacement 
    
    public static SpeedController moteurDeplacementAvantDroite; //declaration du moteur de deplacement avant droit
    public static SpeedController moteurDeplacementAvantGauche;//declaration du moteur de deplacement avant gauche
    public static SpeedController moteurDeplacementArriereDroite;//declaration du moteur de deplacement arriere droit
    public static SpeedController moteurDeplacementArriereGauche;//declaration du moteur de deplacement arriere gauche
    
    //tourelle
    public static SpeedController moteurTourelle; //declaration du moteur de la tourelle
    
    //compresseur
  //  AnalogInput pressureSensor;
	Compressor compressor;
	
	//Pistons pour lever le shooter
	DoubleSolenoid pistonGauche;
	DoubleSolenoid pistonDroite;
	
	
	//Piston du shooter
	DoubleSolenoid pistonShooter;
	
	//Piston du chargeur
    DoubleSolenoid pistonChargeur; //definir les ports plus tard
	
	
	//si on definit un relais
	//Relay  spike ;   
	//Joysticks definition
    Joystick joystickPilote;//declaration du joystick du pilote
    Joystick joystickCoPilote;//declaration du joystick du copilote
    //various constants definition
    final double NORMALSPEED  = 0.75;  //drive for normal speed
    final double ROTATESPEED    = 0.625;  //drive motor slow speed
    final boolean SENSITIVITY = true; // drive motor sensibility
    
    final double GAUCHETOURELLE  = 0.7;
    final double DROITETOURELLE  = -0.7;
    final double ARRETETOURELLE  = 0;
      
   // private static final double MAX_PRESSURE = 2.55;
    
    //camera definition
    Image frame;
    AxisCamera axisCamera;
	//Autonomous mode definition
    final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    SendableChooser chooser;

    public Robot() {
    	moteurDeplacementAvantDroite = new Talon(2);
        moteurDeplacementAvantGauche = new Talon(0);
        moteurDeplacementArriereDroite = new Talon(3);
        moteurDeplacementArriereGauche  = new Talon(1);
        
        myRobot = new RobotDrive(moteurDeplacementAvantGauche, moteurDeplacementArriereGauche,
				   moteurDeplacementAvantDroite, moteurDeplacementArriereDroite); 
        
        myRobot.setSafetyEnabled(true);
        myRobot.setExpiration(0.1);
        myRobot.setSensitivity(0.5);
        myRobot.setMaxOutput(1.0);
      
       // pressureSensor = new AnalogInput(2);
        compressor = new Compressor(0);
        
        pistonGauche = new DoubleSolenoid(0, 4, 5);//Module number Forward Channel Reverse Channel
		pistonDroite = new DoubleSolenoid(0, 6, 7);
		//S
		pistonShooter = new DoubleSolenoid(0, 2, 3);//Module number i.e. the CAN ID The Channel on the PCM control
		pistonChargeur = new DoubleSolenoid(0, 0, 1);
		//spike = new Relay(7);
        
        joystickPilote = new Joystick(0);
        joystickCoPilote = new Joystick(1);
        
        moteurTourelle = new Talon(4);
        
        
    }
    
    public void robotInit() {
    	compressor.start();
    	boolean ActivationCompresseur = compressor.enabled();
    	boolean pressureSwitch = compressor.getPressureSwitchValue();
    	float current = compressor.getCompressorCurrent();
    	
    	//affichage etat du compresseur
    	SmartDashboard.putBoolean("Compresseur Actif", ActivationCompresseur);
    	SmartDashboard.putBoolean("pressureSwitch", pressureSwitch);
    	SmartDashboard.putNumber("Current", current);
    	
    	frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);

         // open the camera at the IP address assigned. This is the IP address that the camera
         // can be accessed through the web interface.
    	axisCamera = new AxisCamera("10.35.50.11");
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto modes", chooser);
    }

	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the if-else structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
    public void autonomous() {
    	
    	String autoSelected = (String) chooser.getSelected();
//		String autoSelected = SmartDashboard.getString("Auto Selector", defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
    	
    	switch(autoSelected) {
    	case customAuto:
            myRobot.setSafetyEnabled(false);
            myRobot.drive(-0.5, 1.0);	// spin at half speed
            Timer.delay(2.0);		//    for 2 seconds
            myRobot.drive(0.0, 0.0);	// stop robot
            break;
    	case defaultAuto:
    	default:
            myRobot.setSafetyEnabled(false);
            myRobot.drive(-0.5, 0.0);	// drive forwards half speed
            Timer.delay(2.0);		//    for 2 seconds
            myRobot.drive(0.0, 0.0);	// stop robot
            break;
    	}
    }

    /**
     * Runs the motors with arcade steering.
     */
    public void operatorControl() {
    	
        myRobot.setSafetyEnabled(true);
        while (isOperatorControl() && isEnabled()) {
        	
        	drive(getjoystickPiloteY(), getjoystickPiloteX()); // pilotage du robot
        	
        	//affichage vitesses des moteurs de deplacement
        	SmartDashboard.putNumber("AvantDroitVitesse", moteurDeplacementAvantDroite.get());
        	SmartDashboard.putNumber("AvantGaucheVitesse", moteurDeplacementAvantGauche.get());
        	SmartDashboard.putNumber("ArriereDroitVitesse", moteurDeplacementArriereDroite.get());
        	SmartDashboard.putNumber("ArriereGaucheVitesse", moteurDeplacementArriereGauche.get());
            //myRobot.arcadeDrive(joystickPilote); // drive with arcade style (use right stick)
        	
        	//affichage des informations utiles sur le joystick du pilote
        	logPiloteJoystick();
        	
        	//affichage des informations utiles sur le joystick du copilote
        	logCoPiloteJoystick();//affichage des boutons du joystick du copilote
        	
        	//activation par le copilote du moteur de rotation de la plateforme
        	if (getJoystickCoPilote().getRawButton(5)) { // a developper en ajoutant eventuellement un delai pour ce moteur et des limites switches
                tourneAdroiteTourelle(); 
                Timer.delay(0.05);
              } else if (getJoystickCoPilote().getRawButton(6)) {
            	  tourneAgaucheTourelle();
            	  Timer.delay(0.05);
              }else
            	  arreteTourelle();
        	SmartDashboard.putNumber("Tourelle", moteurTourelle.get());
        	//   Timer.delay(0.002);		// voir si ce timer uniquement pour la tourelle est indispensable 
      //  }utile si le timer de la tourelle est necessaire
        	
        	//activation par le copilote des differents solenoides
        if (getJoystickCoPilote().getRawButton(3)) { // a developper en ajoutant eventuellement un delai pour ce moteur et des limites switches
        		extendBoth();
        }else if (getJoystickCoPilote().getRawButton(4)){
        	 retractBoth();
              }else
            	  desactivateBoth();
        
        if (getJoystickCoPilote().getRawButton(2)) { // a developper en ajoutant eventuellement un delai pour ce moteur et des limites switches
        	pistonChargeur.set(DoubleSolenoid.Value.kReverse);
        }else if(getJoystickCoPilote().getRawButton(1)){
        	//SmartDashboard.putBoolean("ChargeurON", joystickCoPilote.getRawButton(2));
        	pistonChargeur.set(DoubleSolenoid.Value.kForward);
        }else
            pistonChargeur.set(DoubleSolenoid.Value.kOff);
        
         if (getJoystickCoPilote().getRawButton(5)) { 
        	 pistonShooter.set(DoubleSolenoid.Value.kReverse);
          }else if(getJoystickCoPilote().getRawButton(6)){ 
        	//SmartDashboard.putBoolean("ShooterON", joystickCoPilote.getRawButton(1));
		    pistonShooter.set(DoubleSolenoid.Value.kForward);
          }else
            pistonShooter.set(DoubleSolenoid.Value.kOff);
		   // SmartDashboard.putBoolean(return pistonShooter.get());
        	 /*if (getJoystickCoPilote().getRawButton(5)){
   			       latch();
   		       }else unlatch();
        	 
        	 if (getJoystickCoPilote().getRawButton(6)){
 			       spike.set(Relay.Value.kForward);
 		       }else if (getJoystickCoPilote().getRawButton(7)){
 		    	  spike.set(Relay.Value.kReverse);
 		       }else
 		    	  spike.set(Relay.Value.kOff);  */
        	//   Timer.delay(0.002);		// voir si ce timer uniquement pour la tourelle est indispensable 
      //  }utile si le timer de la tourelle est necessaire
          //  Timer.delay(0.005);		// timer de l'ensemble des moteur necessaire ou non
        }
    }

    /**
     * Runs during test mode
     */
    public void test() {
    }
     
   /*
    *Cette section comprend les fonctions developpees pour le pilotage et les differentes
    *actions que le robot entreprend ainsi que l'affichage des donnees utiles sur le 
    *Smartdashboard
    * 
    * 
    */
    /**
	 * The log method puts interesting information to the SmartDashboard.
	 */
	public void logSystem() {
	//	SmartDashboard.putNumber("Left Distance", m_leftEncoder.getDistance());
	//	SmartDashboard.putNumber("Right Distance", m_rightEncoder.getDistance());
	//	SmartDashboard.putNumber("Left Speed", m_leftEncoder.getRate());
	//	SmartDashboard.putNumber("Right Speed", m_rightEncoder.getRate());
	//	SmartDashboard.putNumber("Gyro", m_gyro.getAngle());
	//	SmartDashboard.putNumber("Pressure", pressureSensor.getVoltage());
	}
		
	private void inverseMotors(boolean inverse) {
		moteurDeplacementAvantGauche.setInverted(inverse);
	    moteurDeplacementArriereGauche.setInverted(inverse);
		moteurDeplacementAvantDroite.setInverted(inverse);
	    moteurDeplacementArriereDroite.setInverted(inverse);
	}
	
	/**
	 * drive method provides a way to explicitly choose the joystick or gamePad axis in order to operate the robot
	 * 
	 * @param moveValue The speed that the robot should drive in the y direction in range [-1.0..1.0]
	 * @param rotateValue The rate of rotation for the robot that is dependent of the translation. [-1.0..1.0]
	 */
	public void drive(double moveValue, double rotateValue) {
		
		inverseMotors(true);
		myRobot.arcadeDrive(NORMALSPEED*moveValue, ROTATESPEED *rotateValue, SENSITIVITY);
	}
	
   public void tourneAgaucheTourelle() {
	    moteurTourelle.set(GAUCHETOURELLE);
	}
   
   public void tourneAdroiteTourelle() {
	    moteurTourelle.set(DROITETOURELLE);
	}
   
   public void arreteTourelle() {
	    moteurTourelle.set(ARRETETOURELLE);
	}
	
   /** 
	 * Extend both solenoids for shooter in UP position.
	 */
	public void extendBoth() {
		pistonDroite.set(DoubleSolenoid.Value.kForward);
		pistonGauche.set(DoubleSolenoid.Value.kForward);
	}

	/**
	 * Retract both solenoids for shooter in DOWN position.
	 */
	public void retractBoth() {
		pistonDroite.set(DoubleSolenoid.Value.kReverse);
		pistonGauche.set(DoubleSolenoid.Value.kReverse);
	}
   
	/**
	 * desactivate both solenoids for shooter in DOWN position.
	 */
	public void desactivateBoth() {
		pistonDroite.set(DoubleSolenoid.Value.kOff);
		pistonGauche.set(DoubleSolenoid.Value.kOff);
	}
	/**
	 * Release the latch so that we can shoot
	 */
	public void unlatch() {
		//pistonShooter.set(true);
	}

	/**
	 * Latch so that pressure can build up and we aren't limited by air flow.
	 */
	public void latch() {
		//pistonShooter.set(false);
	}
	
	/**
	 * Latch so that pressure can build up and we aren't limited by air flow.
	 */
	//public boolean getSolenoid() {
		//return pistonDroite.get();
	//}


	public double getjoystickPiloteX() { //x axis on the gamePadPilote right joystick
		return joystickPilote.getRawAxis(2);
		//return joystickPilote.getRawAxis(0); //jeremie
	}
	
	public double getjoystickPiloteY() { //y axis on the gamePadPilote left joystick
		//return joystickPilote.getRawAxis(2); //jeremie
		return joystickPilote.getRawAxis(1);
	}
	
	public Joystick getJoystickCoPilote() { //CoPilote joystick on usb1
		return joystickCoPilote;
	}
	
	public void logCoPiloteJoystick() {
		SmartDashboard.putBoolean("ShooterON", joystickCoPilote.getRawButton(1));
		SmartDashboard.putBoolean("ChargeurON", joystickCoPilote.getRawButton(2));
		SmartDashboard.putBoolean("PistonsLaterauxUP", joystickCoPilote.getRawButton(3));
		SmartDashboard.putBoolean("PistonsLaterauxDown", joystickCoPilote.getRawButton(4));
		SmartDashboard.putBoolean("PlateformeGauche", joystickCoPilote.getRawButton(5));
		SmartDashboard.putBoolean("PlateformeDroite", joystickCoPilote.getRawButton(6));
	}
	
	public void logPiloteJoystick() {
		SmartDashboard.putNumber("PiloteAxisDX", joystickPilote.getRawAxis(0));
		SmartDashboard.putNumber("PiloteAxisDY", joystickPilote.getRawAxis(1));
		SmartDashboard.putNumber("PiloteAxisGY", joystickPilote.getRawAxis(2));
		SmartDashboard.putNumber("PiloteAxisD_ROTATE", joystickPilote.getRawAxis(3));
		SmartDashboard.putNumber("PiloteAxisGX", joystickPilote.getRawAxis(4));
		
	}
}
