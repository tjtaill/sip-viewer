package javax.sip.viewer.eclipse.plugins.popup.actions;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sip.viewer.SipTextViewer;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;

public class SipViewerPlugin implements IObjectActionDelegate {

  private Shell shell;
  private ISelection selection;

  /**
   * Constructor for Action1.
   */
  public SipViewerPlugin() {
    super();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    shell = targetPart.getSite().getShell();
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    try {
      SipTextViewer lSipTextViewer = new SipTextViewer();
      lSipTextViewer.setParserClassName(SipLogAdapterParser.class.getCanonicalName());

      List<String> lFilesURIs = new ArrayList<String>();

      IStructuredSelection treeSelection = (IStructuredSelection) selection;
      Iterator<IResource> resourceIter = treeSelection.iterator();
      while (resourceIter.hasNext()) {
        IResource resource = (IResource) resourceIter.next();
        lFilesURIs.add(resource.getLocation().toString());
      }

      lSipTextViewer.setFileNames(lFilesURIs);

      MessageConsole console = findConsole("sip-viewer");
      // console.clearConsole(); // bug with clear console....
      if (console.getDocument() != null) {
        console.getDocument().set("");
      }
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      String id = IConsoleConstants.ID_CONSOLE_VIEW;
      IConsoleView view = (IConsoleView) page.showView(id);
      view.setScrollLock(true);

      view.display(console);
      OutputStream output = console.newOutputStream();
      lSipTextViewer.display(output);
    } catch (Exception e) {
      e.printStackTrace();
      MessageDialog.openInformation(shell, "sip-viewer-plugin", e.toString());
    }
  }

  private MessageConsole findConsole(String name) {
    ConsolePlugin plugin = ConsolePlugin.getDefault();
    IConsoleManager conMan = plugin.getConsoleManager();
    IConsole[] existing = conMan.getConsoles();
    for (int i = 0; i < existing.length; i++) {
      if (name.equals(existing[i].getName())) {
        return (MessageConsole) existing[i];
      }
    }
    // no console found -> create new one
    MessageConsole newConsole = new MessageConsole(name, null);
    conMan.addConsoles(new IConsole[] { newConsole });
    return newConsole;
  }

  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
  }

}
