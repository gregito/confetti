package org.confetti.rcp.constraints;

import org.confetti.rcp.extensions.ConstraintDescr;
import org.confetti.rcp.extensions.ConstraintField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Gabor Bubla
 */
public class ConstraintDialog extends Dialog {

    private final ConstraintDescr descr;

    public ConstraintDialog(Shell parentShell, ConstraintDescr descr) {
        super(parentShell);
        this.descr = descr;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(2, false));
        for (ConstraintField field : descr.getFields()) {
            Label label = new Label(area, SWT.NONE);
            label.setText(field.getLabel());
            field.createControl(area);
        }
        return area;
    }
    
}
