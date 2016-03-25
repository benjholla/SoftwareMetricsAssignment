package metrics.ui;

import static com.ensoftcorp.atlas.core.script.Common.extend;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.query.Attr.Edge;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;


public class UIUtils {

    /**
     * Shows the Atlas graph
     * @param q
     * @param h
     * @param extend
     * @param title
     */
    public static void show(Q q, Highlighter h, boolean extend, String title) {
            if (h == null) h = new Highlighter();
            Q displayExpr = extend ? extend(q, Edge.DECLARES) : q;
            DisplayUtil.displayGraph(displayExpr.eval(), h, title);
    }

    /**
     * Displays an alert dialog to the user
     * @param title
     * @param message
     */
    public static void showMessage(String title, String message) {
            MessageDialog.openInformation(Display.getDefault().getActiveShell(), title, message);
    }
    
    /**
	 * Makes a display prompt alerting the user of the error and offers the
	 * ability to copy a stack trace to the clipboard
	 * @param t
	 * @param message
	 */
	public final static void showError(final Throwable t, final String message) {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				// couldn't get the project directories
				MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.NO | SWT.YES);
				mb.setText("Alert");
				StringWriter errors = new StringWriter();
				t.printStackTrace(new PrintWriter(errors));
				String stackTrace = errors.toString();
				mb.setMessage(message + "\n\nWould you like to copy the stack trace?");
				int response = mb.open();
				if (response == SWT.YES) {
					StringSelection stringSelection = new StringSelection(stackTrace);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, stringSelection);
				}
			}
		});
	}
	
}
