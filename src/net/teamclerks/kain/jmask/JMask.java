package net.teamclerks.kain.jmask;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import net.teamclerks.kain.jmask.dialog.CPPrompt;
import net.teamclerks.kain.jmask.maskaction.MaskAction;
import net.teamclerks.kain.jmask.panel.JMaskPanel;
import net.teamclerks.kain.masks.Mask;
import net.teamclerks.kain.masks.exception.MaskException;
import net.teamclerks.kain.masks.impl.CP;
import net.teamclerks.kain.masks.impl.FL;
import net.teamclerks.kain.masks.impl.Flip;
import net.teamclerks.kain.masks.impl.Glass;
import net.teamclerks.kain.masks.impl.Meko;
import net.teamclerks.kain.masks.impl.Negative;
import net.teamclerks.kain.masks.impl.Q0;
import net.teamclerks.kain.masks.impl.RGBRotate;
import net.teamclerks.kain.masks.impl.Win;
import net.teamclerks.kain.masks.impl.Xor;
import net.teamclerks.kain.masks.type.Type;


/**
 * Nanoshop is a tiny tiny tiny (hence nano) image processing program.
 * It has no user interface beyond a file dialog, but it does have the
 * core pieces around which a "real" image processing program can be
 * built.
 */

public class JMask extends JFrame implements ActionListener
{
  private static final long serialVersionUID = -7235844065724493647L;
  
  private static final int ZOOM_MULTIPLIER_MAX = 3;
  private static final int ZOOM_MULTIPLIER_MIN = 1;
  
  private JMaskPanel panel;
    
  private JDialog popup = null;
  
  private CPPrompt prompt;
  
  private Stack<MaskAction> actions;
  
  private Stack<MaskAction> redoActions;
  
  private JMenuBar buttons;

  /**
   * Initializes a Nanoshop panel.
   */
  public JMask(String name)
  {
    super(name);
    panel = new JMaskPanel();
    
    JScrollPane scroll = new JScrollPane(panel);

    this.addMenu();
    
    add(buttons, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
    
    this.setSize(640,480);
    this.setPreferredSize(new Dimension(640,480));
    
    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    panel.setVisible(true);
    panel.addMouseListener(panel);
    panel.addMouseMotionListener(panel);
    actions = new Stack<MaskAction>();
    redoActions = new Stack<MaskAction>();
  }
  
  /**
   * Sets the file containing the image to display.
   *
   * @param file the File containing the image.
   */
  private void setFile(File file)
  {
    try
    {
      setImage(ImageIO.read(file));
    } 
    catch(IOException ioexc)
    {
      // No-op for now; we just don't set the image.
      // In reality, there should be some good user notification here.
    }
    catch(NullPointerException npe)
    {
      // Not really sure why... but sometimes there is a non-fatal
      // NPE thrown here.
    }
  }
  
  /**
   * Sets the image to display.
   *
   * @param image the image to display
   */
  private void setImage(BufferedImage image)
  {
    actions = new Stack<MaskAction>();
    panel.setImage(image);
    panel.revalidate();
    for(Component c: buttons.getComponents())
    {
      if(c instanceof JButton)
      {
        c.setEnabled(true);
      }
    }
    repaint();
  }
  
  
  /**
   * @see java.awt.Component#getPreserredSize
   */
  public Dimension getPreferredSize()
  {
    if(panel.getImage() != null)
      return(new Dimension(panel.getImage().getWidth(), panel.getImage().getHeight()));
    else
      return(super.getPreferredSize());
  }
  
  public void actionPerformed(ActionEvent e)
  {
    String _button = new String(e.getActionCommand());

    if ( _button.equals("Quit"))
    {
      System.exit(0);
    }
    
    Object object = e.getSource();
    if (prompt != null  && object == prompt.getCancel())
    {
      prompt.setCode("");
      prompt.setVisible(false);
    }
    
    if ( _button.equals("Open File..."))
    {
      //ask for an image file to open.
      FileDialog fd = new FileDialog(this, "Open File", FileDialog.LOAD);
      fd.setVisible(true);
      
      if(fd.getFile() != null)
      {
        //Set the image to display.
        this.setFile(new File(fd.getDirectory(), fd.getFile()));
        // Now that we have a file, we can close it.
        ((JMenu)this.getJMenuBar().getComponents()[0]).getMenuComponent(1).setEnabled(true);
        ((JMenu)this.getJMenuBar().getComponents()[0]).getMenuComponent(2).setEnabled(true);
        ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(0).setEnabled(true);
        ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(1).setEnabled(true);
      }
    }
    
    if ( _button.equals("Save As..."))
    {
      //ask for an image file to open.
      FileDialog fd = new FileDialog(this, "Save As...", FileDialog.SAVE);
      fd.setVisible(true);
      
      if(fd.getFile() != null)
      {
        //Set the image to display.
        File output = new File(fd.getDirectory(), fd.getFile());
        String format = output.getName().lastIndexOf('.') > 0 ? 
            output.getName().substring(output.getName().lastIndexOf('.') + 1) :
            "png";
        try
        {
          ImageIO.write(panel.getImage(), format, new File(output.getAbsolutePath()));
        }
        catch(IOException ioe)
        {
          // This should be handled better...
          // ignore for now
        }
      }
    }
    
    if ( _button.equals("OK!") && popup != null)
    {
      popup.dispose();
      popup = null;
    }
    
    try
    {
      if(panel.getImage() == null) return;
      
      if ( _button.equals("North+"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x, rectToDraw.y-1, rectToDraw.width, rectToDraw.height+1);
      }
      if ( _button.equals("North-"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x, rectToDraw.y+1, rectToDraw.width, rectToDraw.height-1);
      }
      if ( _button.equals("East+"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x, rectToDraw.y, rectToDraw.width+1, rectToDraw.height);
      }
      if ( _button.equals("East-"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x, rectToDraw.y, rectToDraw.width-1, rectToDraw.height);
      }
      if ( _button.equals("South+"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x, rectToDraw.y, rectToDraw.width, rectToDraw.height+1);
      }
      if ( _button.equals("South-"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x, rectToDraw.y, rectToDraw.width, rectToDraw.height-1);
      }
      if ( _button.equals("West+"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x-1, rectToDraw.y, rectToDraw.width+1, rectToDraw.height);
      }
      if ( _button.equals("West-"))
      {
        Rectangle rectToDraw = panel.getRectangle();
        if(rectToDraw == null) return;
        panel.trimBox(rectToDraw.x+1, rectToDraw.y, rectToDraw.width-1, rectToDraw.height);
      }
      
      if ( _button.equals("Zoom In"))
      {
        if(panel.getZoomMultiplier() < ZOOM_MULTIPLIER_MAX)
        {
          ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(1).setEnabled(true);
          panel.setZoomMultiplier(panel.getZoomMultiplier()+1);
          panel.setMaximumSize(new Dimension(panel.getWidth()*panel.getZoomMultiplier(),panel.getHeight()*panel.getZoomMultiplier()));
          if(panel.getZoomMultiplier() == ZOOM_MULTIPLIER_MAX)
          {
            ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(0).setEnabled(false);
          }
          panel.revalidate();
          repaint();
        }
      }
      if ( _button.equals("Zoom Out"))
      {
        if(panel.getZoomMultiplier() > ZOOM_MULTIPLIER_MIN)
        {
          ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(0).setEnabled(true);
          panel.setZoomMultiplier(panel.getZoomMultiplier()-1);
          panel.setMaximumSize(new Dimension(panel.getWidth()*panel.getZoomMultiplier(),panel.getHeight()*panel.getZoomMultiplier()));
          if(panel.getZoomMultiplier() == ZOOM_MULTIPLIER_MIN)
          {
            ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(1).setEnabled(false);
          }
          panel.revalidate();
          repaint();
        }
      }
      if ( _button.equals("Undo"))
      {
        if(actions.size() == 1)
        {
          ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(false);
        }
        // Bam... undo
        MaskAction undo = actions.pop();
        int zoomMultilplier = panel.getZoomMultiplier();
        panel.setZoomMultiplier(1);
        panel.setImage(undo.getMask().undoMask());
        panel.setZoomMultiplier(zoomMultilplier);
        redoActions.push(undo);
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Redo"))
      {
        if(redoActions.size() == 1)
        {
          ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        }
        // Bam... redo
        MaskAction redo = redoActions.pop();
        int zoomMultilplier = panel.getZoomMultiplier();
        panel.setZoomMultiplier(1);
        panel.setImage(redo.getMask().mask());
        panel.setZoomMultiplier(zoomMultilplier);
        actions.push(redo);
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }

      if (prompt != null && object == prompt.getOkay())
      {
        prompt.setVisible(false);
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new CP(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_CP);
        ((CP)mask).setCode(prompt.getCode());
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        prompt.setCode("");
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("RGB Rotate"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new RGBRotate(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_ROTATE_RGB);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("XOR"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Xor(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_XOR);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Horizontal Flip"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Flip(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_HFLIP);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Vertical Flip"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Flip(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_VFLIP);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Negative"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Negative(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_NEGATIVE);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Vertical Glass"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Glass(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_VGLASS);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Horizontal Glass"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Glass(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_HGLASS);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Win"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Win(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_WIN);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Meko-"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Meko(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_MEKO_MINUS);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Meko+"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Meko(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_MEKO_PLUS);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("FL"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new FL(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_FL);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("Q0"))
      {
        Rectangle box = panel.getScaledRectangle();
        int x1 = box.x+box.width;
        int y1 = box.y+box.height;
        Mask mask = new Q0(panel.getScaledImage(),box.x,box.y,x1,y1);
        mask.setType(Type.MASK_TYPE_Q0);
        panel.setImage(mask.mask());
        actions.push(new MaskAction(mask,panel.getScaledRectangle()));
        // Always clear the redo stack
        redoActions.clear();
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(1).setEnabled(false);
        // Enable the undo menu item
        ((JMenu)this.getJMenuBar().getComponents()[1]).getMenuComponent(0).setEnabled(true);
        panel.repaint();
      }
      if ( _button.equals("CP-PROMPT"))
      {
        prompt = new CPPrompt(this);
        prompt.setVisible(true);
      }
    }
    catch(MaskException me)
    {
      me.printStackTrace();
    }
    
    if ( _button.equals("Close File"))
    {
      actions = new Stack<MaskAction>();
      panel.setImage(null);
      panel.revalidate();
      ((JMenu)this.getJMenuBar().getComponents()[0]).getMenuComponent(1).setEnabled(false);
      ((JMenu)this.getJMenuBar().getComponents()[0]).getMenuComponent(2).setEnabled(false);
      ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(0).setEnabled(false);
      ((JMenu)this.getJMenuBar().getComponents()[2]).getMenuComponent(1).setEnabled(false);
      repaint();
    }
  }
  
  private void addMenu()
  {
    //Create a menubar
    JMenuBar _menuBar = new JMenuBar();
    
    JMenuBar _menu2 = new JMenuBar();
    
    //Build the first menu.
    JMenu _menu = new JMenu("File");
    _menuBar.add(_menu);
    
    JMenuItem _open = new JMenuItem("Open File...");
    _open.addActionListener(this);
    _open.setAccelerator(KeyStroke.getKeyStroke(
        'O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    _menu.add(_open);
    
    JMenuItem _close = new JMenuItem("Save As...");
    _close.addActionListener(this);
    _close.setEnabled(false);
    _close.setAccelerator(KeyStroke.getKeyStroke(
        'S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    _menu.add(_close);
    
    JMenuItem _save = new JMenuItem("Close File");
    _save.addActionListener(this);
    _save.setEnabled(false);
    _save.setAccelerator(KeyStroke.getKeyStroke(
        'W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    _menu.add(_save);
    
    JSeparator _blank = new JSeparator();
    _menu.add(_blank);
    
    JMenuItem _exit = new JMenuItem("Quit");
    _exit.addActionListener(this);
    _exit.setAccelerator(KeyStroke.getKeyStroke(
        'Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    _menu.add(_exit);
    
    //Build the second menu.
    JMenu editmenu = new JMenu("Edit");
    _menuBar.add(editmenu);
    
    JMenuItem _undo = new JMenuItem("Undo");
    _undo.addActionListener(this);
    _undo.setAccelerator(KeyStroke.getKeyStroke(
        'Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    _undo.setEnabled(false);
    editmenu.add(_undo);
    
    JMenuItem _redo = new JMenuItem("Redo");
    _redo.addActionListener(this);
    _redo.setAccelerator(KeyStroke.getKeyStroke(
        'Y', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    _redo.setEnabled(false);
    editmenu.add(_redo);

    //Build the third menu.
    JMenu zoomMenu = new JMenu("Zoom");
    _menuBar.add(zoomMenu);
    
    JMenuItem zoomIn = new JMenuItem("Zoom In");
    zoomIn.addActionListener(this);
    zoomIn.setAccelerator(KeyStroke.getKeyStroke(
        '=', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    zoomIn.setEnabled(false);
    zoomMenu.add(zoomIn);
    
    JMenuItem zoomOut = new JMenuItem("Zoom Out");
    zoomOut.addActionListener(this);
    zoomOut.setAccelerator(KeyStroke.getKeyStroke(
        '-', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    zoomOut.setEnabled(false);
    zoomMenu.add(zoomOut);

    //Build the third menu.
    JMenu trimMenu = new JMenu("Trim");
    _menuBar.add(trimMenu);
    
    JMenuItem trimNorthUp = new JMenuItem("North+");
    trimNorthUp.addActionListener(this);
    trimNorthUp.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimNorthUp);
    
    JMenuItem trimNorthDown = new JMenuItem("North-");
    trimNorthDown.addActionListener(this);
    trimNorthDown.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_NUMPAD8, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimNorthDown);
    
    JMenuItem trimEastUp = new JMenuItem("East+");
    trimEastUp.addActionListener(this);
    trimEastUp.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimEastUp);
    
    JMenuItem trimEastDown = new JMenuItem("East-");
    trimEastDown.addActionListener(this);
    trimEastDown.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_NUMPAD6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimEastDown);
    
    JMenuItem trimSouthUp = new JMenuItem("South+");
    trimSouthUp.addActionListener(this);
    trimSouthUp.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimSouthUp);
    
    JMenuItem trimSouthDown = new JMenuItem("South-");
    trimSouthDown.addActionListener(this);
    trimSouthDown.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_NUMPAD5, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimSouthDown);
    
    JMenuItem trimWestUp = new JMenuItem("West+");
    trimWestUp.addActionListener(this);
    trimWestUp.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimWestUp);
    
    JMenuItem trimWestDown = new JMenuItem("West-");
    trimWestDown.addActionListener(this);
    trimWestDown.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_NUMPAD4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),false) );
    trimMenu.add(trimWestDown);
    
    JButton _rgbRot = new JButton();
    _rgbRot.setActionCommand("RGB Rotate");
    _rgbRot.setIcon(new ImageIcon("resources/images/rgb.png"));
    _rgbRot.setPreferredSize(new Dimension(27,27));
    _rgbRot.addActionListener(this);
    _rgbRot.setEnabled(false);
    _menu2.add(_rgbRot);

    JButton _xor = new JButton();
    _xor.setActionCommand("XOR");
    _xor.setIcon(new ImageIcon("resources/images/xor.png"));
    _xor.setPreferredSize(new Dimension(27,27));
    _xor.addActionListener(this);
    _xor.setEnabled(false);
    _menu2.add(_xor);

    JButton _hflip = new JButton();
    _hflip.setActionCommand("Horizontal Flip");
    _hflip.setIcon(new ImageIcon("resources/images/hflip.png"));
    _hflip.setPreferredSize(new Dimension(27,27));
    _hflip.addActionListener(this);
    _hflip.setEnabled(false);
    _menu2.add(_hflip);

    JButton _vflip = new JButton();
    _vflip.setActionCommand("Vertical Flip");
    _vflip.setIcon(new ImageIcon("resources/images/vflip.png"));
    _vflip.setPreferredSize(new Dimension(27,27));
    _vflip.addActionListener(this);
    _vflip.setEnabled(false);
    _menu2.add(_vflip);
   
    JButton _neg = new JButton();
    _neg.setActionCommand("Negative");
    _neg.setIcon(new ImageIcon("resources/images/neg.png"));
    _neg.setPreferredSize(new Dimension(27,27));
    _neg.addActionListener(this);
    _neg.setEnabled(false);
    _menu2.add(_neg);
    
    JButton _vglass = new JButton();
    _vglass.setActionCommand("Vertical Glass");
    _vglass.setIcon(new ImageIcon("resources/images/vglass.png"));
    _vglass.setPreferredSize(new Dimension(27,27));
    _vglass.addActionListener(this);
    _vglass.setEnabled(false);
    _menu2.add(_vglass);
    
    JButton _hglass = new JButton();
    _hglass.setActionCommand("Horizontal Glass");
    _hglass.setIcon(new ImageIcon("resources/images/hglass.png"));
    _hglass.setPreferredSize(new Dimension(27,27));
    _hglass.addActionListener(this);
    _hglass.setEnabled(false);
    _menu2.add(_hglass);
    
    JButton _win = new JButton();
    _win.setActionCommand("Win");
    _win.setIcon(new ImageIcon("resources/images/win.png"));
    _win.setPreferredSize(new Dimension(27,27));
    _win.addActionListener(this);
    _win.setEnabled(false);
    _menu2.add(_win);
    
    JButton _mekoMinus = new JButton();
    _mekoMinus.setActionCommand("Meko-");
    _mekoMinus.setIcon(new ImageIcon("resources/images/mekoMinus.png"));
    _mekoMinus.setPreferredSize(new Dimension(27,27));
    _mekoMinus.addActionListener(this);
    _mekoMinus.setEnabled(false);
    _menu2.add(_mekoMinus);
    
    JButton _mekoPlus = new JButton();
    _mekoPlus.setActionCommand("Meko+");
    _mekoPlus.setIcon(new ImageIcon("resources/images/mekoPlus.png"));
    _mekoPlus.setPreferredSize(new Dimension(27,27));
    _mekoPlus.addActionListener(this);
    _mekoPlus.setEnabled(false);
    _menu2.add(_mekoPlus);
    
    JButton _fl = new JButton();
    _fl.setActionCommand("FL");
    _fl.setIcon(new ImageIcon("resources/images/fl.png"));
    _fl.setPreferredSize(new Dimension(27,27));
    _fl.addActionListener(this);
    _fl.setEnabled(false);
    _menu2.add(_fl);
    
    JButton _q0 = new JButton();
    _q0.setActionCommand("Q0");
    _q0.setIcon(new ImageIcon("resources/images/q0.png"));
    _q0.setPreferredSize(new Dimension(27,27));
    _q0.addActionListener(this);
    _q0.setEnabled(false);
    _menu2.add(_q0);
    
    JButton _cp = new JButton();
    _cp.setActionCommand("CP-PROMPT");
    _cp.setIcon(new ImageIcon("resources/images/cp.png"));
    _cp.setPreferredSize(new Dimension(27,27));
    _cp.addActionListener(this);
    _cp.setEnabled(false);
    _menu2.add(_cp);

    this.setJMenuBar(_menuBar);
    buttons = _menu2;
  } 
  
  /**
   * Small driver program. Ideally, fuctions like file opening and frame
   * management should also be part of Nanoshop proper.
   */
  
  public static void main(String[] args)
  {
    JMask jmask = new JMask("JMask");
    jmask.setVisible(true);
  }
}
