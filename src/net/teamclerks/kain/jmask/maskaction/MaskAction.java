package net.teamclerks.kain.jmask.maskaction;

import java.awt.Rectangle;

import net.teamclerks.kain.masks.Mask;

public class MaskAction
{
  private Mask mask;
  private Rectangle rect;
  
  protected MaskAction(){}
  
  public MaskAction(Mask mask, Rectangle rect)
  {
    this.mask = mask;
    this.rect = rect;
  }

  public Mask getMask()
  {
    return mask;
  }

  public void setMask(Mask mask)
  {
    this.mask = mask;
  }

  public Rectangle getRect()
  {
    return rect;
  }

  public void setRect(Rectangle rect)
  {
    this.rect = rect;
  }
}
