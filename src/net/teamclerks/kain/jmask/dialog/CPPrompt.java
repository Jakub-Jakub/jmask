package net.teamclerks.kain.jmask.dialog;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.teamclerks.kain.jmask.JMask;

public class CPPrompt extends JDialog 
{
  private static final long serialVersionUID = -2988503434007372218L;

  public CPPrompt(JMask parent)
  {
    super(parent, true);

    //INIT_CONTROLS
    setTitle("CP Mask");
    getContentPane().setLayout(null);
    setSize(403, 129);
    setLocation(parent.getLocation().x, parent.getLocation().y);
    setVisible(false);
    JLabel1.setText("Code:");
    getContentPane().add(JLabel1);
    JLabel1.setBounds(12, 12, 48, 24);
    code.setText("");
    getContentPane().add(code);
    code.setBounds(72, 12, 324, 24);
    ok.setText("OK");
    getContentPane().add(ok);
    ok.setBounds(60, 84, 84, 24);
    cancel.setText("Cancel");
    getContentPane().add(cancel);
    cancel.setBounds(264, 84, 84, 24);

    //REGISTER_LISTENERS
    ok.addActionListener(parent);
    cancel.addActionListener(parent);
  }

  public void setVisible(boolean b) {
    if (b)
      setLocation(50, 50);
    super.setVisible(b);
  }

  public void addNotify() 
  {
    // Record the size of the window prior to calling parents addNotify.
    Dimension size = getSize();

    super.addNotify();

    if (frameSizeAdjusted)
      return;
    frameSizeAdjusted = true;

    // Adjust size of frame according to the insets
    Insets insets = getInsets();
    setSize(insets.left + insets.right + size.width, insets.top
        + insets.bottom + size.height);
  }

  // Used by addNotify
  boolean frameSizeAdjusted = false;

  //DECLARE_CONTROLS
  JLabel JLabel1 = new JLabel();

  JLabel JLabel2 = new JLabel();

  /**
   * The user ID entered.
   */
  private JTextField code = new JTextField();

  private JButton ok = new JButton();

  private JButton cancel = new JButton();

  public void setCode(String code)
  {
    this.code.setText(code);
  }
  
  public String getCode()
  {
    return code.getText();
  }
  
  public JButton getOkay()
  {
    return ok;
  }
  
  public JButton getCancel()
  {
    return cancel;
  }
}
