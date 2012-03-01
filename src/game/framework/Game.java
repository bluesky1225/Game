package game.framework;
import game.debug.PerformanceTiming;
import game.input.Keyboard;
import game.input.Mouse;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public abstract class Game extends JFrame
{
    // Window Initalization Vars
    private Canvas canvas;
    private BufferStrategy buffer;
    private GraphicsEnvironment ge;
    private GraphicsDevice gd;
    private GraphicsConfiguration gc;
    private BufferedImage bi;
    // Window Settings
    public int width;
    public int height;
    // Game Loop Variables
    private boolean running;
    private Color background; // Background color to clear too
    private GameTime gameTime; // Keeps Track of time in the game
    // Debug Variable (User needs to implement update and draw if they want to use)
    protected PerformanceTiming fps;
    
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Full Constructor
     * Sets game window title, width, and height.
     * @param title String containing the title of the window.
     * @param width Integer that represents the width of the screen.
     * @param height Integer that represents the height of the screen.
     */
    public Game(String title, int width, int height)
    {
        this.setTitle(title);
        this.fps = new PerformanceTiming();
        this.width = width;
        this.height = height;
    }
    
    /**
     * Sets default window title to "Game"
     * @param width Integer that represents the width of the screen.
     * @param height Integer that represents the height of the screen.
     */
    public Game(int width, int height)
    {
        this("Game", width, height);
    }
    
    /**
     * Sets the window title to String provided.
     * Sets default window dimensions to 640 x 480.
     * @param title String containing the title of the window.
     */
    public Game(String title)
    {
        this(title, 640, 480);
    }
    
    /**
     * Empty Constructor.
     * Sets default window Dimensions to 640 x 480.
     * Sets default window title to "Game"
     */
    public Game()
    {
        this("Game", 640, 480);
    }
    //</editor-fold>
        
    //<editor-fold defaultstate="collapsed" desc="Run">
    /**
     * Initialize Variables and starts up GameLoop
     */
    public final void run()
    {
        try
        {
            // Creates the game window and double buffer
            createWindow();
            // Initialize anything if you need too
            initialize();
            // Start the game loop
            gameLoop();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            System.exit(0);
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Game Loop">
    /**
     * Everything that happens in the program happens here. <br />
     * Input -> Game Logic -> Draw <br />
     * ^                        | <br />
     * |________________________|
     */
    public final void gameLoop()
    {
        // Objects needed for rendering...
        Graphics graphics = null;
        Graphics2D g2d = null;
        // If you need to load any content now is the time to do so.
        loadContent();
        // Reset GameTime
        gameTime.reset();
        while(running)
        {
            try
            {
                // Update The GameTime
                gameTime.tick();
                // Update Game Logic
                update(gameTime);
                // clear back buffer...
                g2d = bi.createGraphics();
                // Draw 
                draw(g2d);
                // Sync Screen For Linux/Mac
                Toolkit.getDefaultToolkit().sync();
                // Blit image and flip...
                graphics = buffer.getDrawGraphics();
                graphics.drawImage(bi, 0, 0, null);
                if(!buffer.contentsLost())
                  buffer.show();
                // Let the OS have a little time...
                Thread.yield();
            }
            finally
            {
                // release resources
                if(graphics != null) 
                    graphics.dispose();
                if(g2d != null) 
                    g2d.dispose();
            }
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Exit Game">
    /**
     * Proper way to exit/quit game
     * You can enter a certain error code to provide exit status text
     * @param errorCode Integer containing the error code. 
     */
    public static void exitGame(int errorCode)
    {
        switch(errorCode)
        {
            case 0:
                System.out.println("Game exited successfully.");
                break;
            case -1:
                System.out.println("Game exited unexpectedly.");
                break;
            default:
                System.out.println("Unknown exit status...");
                System.out.println("Quitting..");
                break;  
        }
        System.out.println("Exit Code: " + errorCode);
        System.exit(errorCode);
    }
    
    /**
     * Default way to exit game.
     * Sets error code to Zero.
     */
    public static void exitGame()
    {
        Game.exitGame(0); // 0 is proper exit code
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Helper Methods">
    /**
     * Creates The Game Window with provided settings
     */
    private void createWindow()
    {
        // Set Frame Propertys
        this.setIgnoreRepaint(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        // Create canvas for painting...
        this.canvas = new Canvas();
        this.canvas.setIgnoreRepaint(true);
        this.canvas.setSize(width, height);
        this.canvas.setPreferredSize(new Dimension(width, height));
        //this.canvas.setEnabled(false);
        // Add canvas to game window...
        this.add(canvas);
        this.pack();
        this.setVisible(true);
        this.requestFocus();
        // Create BackBuffer...
        this.canvas.createBufferStrategy(2);
        this.buffer = canvas.getBufferStrategy();
        // Get graphics configuration...
        this.ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.gd = ge.getDefaultScreenDevice();
        this.gc = gd.getDefaultConfiguration();
        // Create off-screen drawing surface
        this.bi = gc.createCompatibleImage(width, height);
        // Set Properties in the GameHelper class
        GameHelper.setScreenWidth(width);
        GameHelper.setScreenHeight(height);
    }
    
    /**
     * Used to set the width of the game window
     * @param width 
     */
    public final void setWidth(int width)
    {
        this.width = width;
        GameHelper.setScreenWidth(width);
    }
    
    /**
     * Used to set the height of the game window
     * @param height 
     */
    public final void setHeight(int height)
    {
        this.height = height;
        GameHelper.setScreenHeight(height);
    }
    
    /**
     * Sets the dimensions of the game window
     * @param width
     * @param height 
     */
    public final void setDimensions(int width, int height)
    {
        this.setWidth(width);
        this.setHeight(height);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Overrideable Game Methods">
    /**
     * Load assets needed for game
     */
    public abstract void loadContent();
    /**
     * Unload assets needed for game
     */
    
    public abstract void unloadContent(); 
    /**
     * Update Human interface states, game logic, etc.
     * Abstract Game handles updating Keyboard and Mouse polling. 
     * @param gameTime GameTime object containing the timing of the current session.
     */
    public void update(GameTime gameTime)
    {
        // Poll the keyboard
        Keyboard.poll(); 
        // Poll the mouse
        Mouse.poll();
    }

    /**
     * Initializes the Window, Keyboard, Mouse, and GameTime objects.    
     */
    public void initialize()
    {
        running = true;
        background = new Color(100, 149, 237); // Default is Cornflower blue
        //keyboard = new KeyboardInput(); // Keyboard polling
        this.addKeyListener(Keyboard.keyboard); // Add Keyboard
        this.canvas.addKeyListener(Keyboard.keyboard);
        //mouse = new MouseInput(); // Mouse input
        this.canvas.addMouseListener(Mouse.mouse);
        this.canvas.addMouseMotionListener(Mouse.mouse);
        gameTime = new GameTime(); // Create GameTime object
    }

    /**
     * Clears and draws the frame.
     * @param g2d Graphics2D object containing the drawable surface of the window. 
     */
    public void draw(Graphics2D g2d)
    {
        // Clear Background
        g2d.setColor(background); // Cornflower blue by default
        g2d.fillRect(0, 0, width, height);
    }
    // </editor-fold>
}
