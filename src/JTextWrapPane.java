import javax.swing.*;
import javax.swing.text.*;

public class JTextWrapPane extends JTextPane {
	private static final long serialVersionUID = -3204357605055969466L;
	boolean wrapState = true;

    public JTextWrapPane() {
        super();
    }

    public JTextWrapPane(StyledDocument p_oSdLog) {
        super(p_oSdLog);
    }


    public boolean getScrollableTracksViewportWidth() {
        return wrapState;
    }


    public void setLineWrap(boolean wrap) {
        wrapState = wrap;
    }


    public boolean getLineWrap(boolean wrap) {
        return wrapState;
    }
}  