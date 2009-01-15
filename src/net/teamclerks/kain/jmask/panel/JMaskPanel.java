package net.teamclerks.kain.jmask.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.event.MouseInputListener;

public class JMaskPanel extends JPanel implements Scrollable, MouseInputListener, MouseMotionListener
{
  private static final long serialVersionUID = 1743279019769279943L;

  private BufferedImage image;
  
  private Rectangle currentRect;

  private Rectangle rectToDraw;

  private Rectangle previousRectDrawn;
  
  public JMaskPanel()
  {
    currentRect = null;
    rectToDraw = null;
    previousRectDrawn = new Rectangle();
  }

  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g); //paints the background and image

    //Not much to do here; just draw the image.
    if(image != null)
    {
      g.drawImage(image,0,0,null);
    }
    //If currentRect exists, paint a box on top.
    if (currentRect != null && rectToDraw != null && image != null) 
    {
      //Draw a rectangle on top of the image.
      g.setXORMode(Color.white); //Color of line varies
      //depending on image colors
      g.drawRect(rectToDraw.x, rectToDraw.y, rectToDraw.width - 1,
          rectToDraw.height - 1);
    }
  }
  
  public Rectangle getRectangle()
  {
    return rectToDraw;
  }
  
  public void setImage(BufferedImage bi)
  {
    image = bi;
    if(bi != null)
      this.setMaximumSize(new Dimension(bi.getWidth(),bi.getHeight()));
  }
  
  public BufferedImage getImage() 
  {
    return image;
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
  }

  public void mousePressed(MouseEvent e)
  {
    if(image == null) return;
    int x = e.getX();
    int y = e.getY();
    if(x >= image.getWidth())
    {
      x = image.getWidth()-1;
    }
    if(y >= image.getHeight())
    {
      y = image.getHeight()-1;
    }
    if(x < 0)
    {
      x = 0;
    }
    if(y < 0)
    {
      y = 0;
    }
    currentRect = new Rectangle(x, y, 0, 0);
    updateDrawableRect(getWidth(), getHeight());
    repaint();
  }

  public void mouseReleased(MouseEvent e)
  {
    updateSize(e);
  }

  public void mouseDragged(MouseEvent e)
  {
    updateSize(e);
  }

  public void mouseMoved(MouseEvent e)
  {
  }
  
  public void updateSize(MouseEvent e) 
  {
    if(image == null) return;
    int x = e.getX();
    int y = e.getY();
    if(x > image.getWidth())
    {
      x = image.getWidth();
    }
    if(y > image.getHeight())
    {
      y = image.getHeight();
    }
    if(currentRect.x < 0)
    {
      currentRect.x = 0;
    }
    if(currentRect.y < 0)
    {
      currentRect.y = 0;
    }
    currentRect.setSize(x - currentRect.x, y - currentRect.y);
    updateDrawableRect(getWidth(), getHeight());
    Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
    repaint(totalRepaint.x, totalRepaint.y, totalRepaint.width,
        totalRepaint.height);
  }
  
  private void updateDrawableRect(int compWidth, int compHeight) 
  {
    int x = (currentRect.x / 8) * 8;
    int y = (currentRect.y / 8) * 8;
    int width = (currentRect.width / 8) * 8;
    int height = (currentRect.height / 8) * 8;

    //Make the width and height positive, if necessary.
    if (width < 0) 
    {
      width = 0 - width;
      x = x - width + 1;
      if (x < 0) 
      {
        width += x;
        x = 0;
      }
    }
    if (height < 0) 
    {
      height = 0 - height;
      y = y - height + 1;
      if (y < 0) 
      {
        height += y;
        y = 0;
      }
    }

    //The rectangle shouldn't extend past the drawing area.
    if ((x + width) > compWidth) 
    {
      width = compWidth - x;
    }
    if ((y + height) > compHeight) 
    {
      height = compHeight - y;
    }

    //Update rectToDraw after saving old value.
    if (rectToDraw != null) 
    {
      previousRectDrawn.setBounds(rectToDraw.x, rectToDraw.y,
          rectToDraw.width, rectToDraw.height);
      rectToDraw.setBounds(x, y, width, height);
    } 
    else 
    {
      rectToDraw = new Rectangle(x, y, width, height);
    }
  }
  
  public Dimension getPreferredSize()
  {
    if(image == null) return new Dimension(0,0);
    return new Dimension(image.getWidth(),image.getHeight());
  }

  public Dimension getPreferredScrollableViewportSize()
  {
    return null;
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect,
      int orientation, int direction)
  {
    return 1;
  }

  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }

  public boolean getScrollableTracksViewportWidth()
  {
    return false;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
      int direction)
  {
    return 10;
  }
}
