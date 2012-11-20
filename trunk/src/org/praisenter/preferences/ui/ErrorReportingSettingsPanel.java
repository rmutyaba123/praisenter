package org.praisenter.preferences.ui;

import java.awt.Graphics;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.praisenter.preferences.ErrorReportingPreferences;
import org.praisenter.preferences.Preferences;
import org.praisenter.resources.Messages;
import org.praisenter.ui.WaterMark;

/**
 * Panel used to setup {@link ErrorReportingPreferences}.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class ErrorReportingSettingsPanel extends JPanel implements PreferencesEditor, ChangeListener {
	/** The version id */
	private static final long serialVersionUID = -4554732355783276924L;
	
	// controls

	/** The check box for enabling reporting */
	private JCheckBox chkReportingEnabled;
	
	/** The check box for enabling SMTP authentication */
	private JCheckBox chkAuthenticateEnabled;
	
	/** The text box for the SMTP host */
	private JTextField txtSmtpHost;
	
	/** The text box for the SMTP port */
	private JFormattedTextField txtSmtpPort;
	
	/** The check box for enabling Start TLS */
	private JCheckBox chkStartTlsEnabled;
	
	/** The text box for the SMTP username */
	private JTextField txtUsername;
	
	/** The text box for the user's email address */
	private JTextField txtEmail;
	
	/**
	 * Default constructor.
	 */
	@SuppressWarnings("serial")
	public ErrorReportingSettingsPanel() {
		Preferences preferences = Preferences.getInstance();
		ErrorReportingPreferences erp = preferences.getErrorReportingPreferences();
		
		boolean enabled = erp.isEnabled();
		
		JLabel lblReportingEnabled = new JLabel(Messages.getString("panel.error.setup.reporting.enabled"));
		this.chkReportingEnabled = new JCheckBox();
		this.chkReportingEnabled.setToolTipText(Messages.getString("panel.error.setup.reporting.enabled.tooltip"));
		this.chkReportingEnabled.setSelected(enabled);
		this.chkReportingEnabled.addChangeListener(this);
		JLabel lblReporting = new JLabel(Messages.getString("panel.error.setup.reporting.description"));
		
		JLabel lblAuthenticateEnabled = new JLabel(Messages.getString("panel.error.setup.reporting.smtp.auth"));
		this.chkAuthenticateEnabled = new JCheckBox();
		this.chkAuthenticateEnabled.setToolTipText(Messages.getString("panel.error.setup.reporting.smtp.auth.tooltip"));
		this.chkAuthenticateEnabled.setSelected(erp.isAuthenticationEnabled());
		this.chkAuthenticateEnabled.setEnabled(enabled);
		
		JLabel lblSmtpHost = new JLabel(Messages.getString("panel.error.setup.reporting.smtp.host"));
		this.txtSmtpHost = new JTextField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// paint a watermark over the text box
				WaterMark.paintTextWaterMark(g, this, Messages.getString("panel.error.setup.reporting.smtp.host.example"));
			}
		};
		this.txtSmtpHost.setToolTipText(Messages.getString("panel.error.setup.reporting.smtp.host.tooltip"));
		this.txtSmtpHost.setText(erp.getSmtpHost());
		this.txtSmtpHost.setColumns(30);
		this.txtSmtpHost.setEnabled(enabled);
		
		JLabel lblSmtpPort = new JLabel(Messages.getString("panel.error.setup.reporting.smtp.port"));
		this.txtSmtpPort = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.txtSmtpPort.setToolTipText(Messages.getString("panel.error.setup.reporting.smtp.port.tooltip"));
		this.txtSmtpPort.setValue(erp.getSmtpPort());
		this.txtSmtpPort.setColumns(5);
		this.txtSmtpPort.setEnabled(enabled);
		
		JLabel lblStartTlsEnabled = new JLabel(Messages.getString("panel.error.setup.reporting.smtp.tls"));
		this.chkStartTlsEnabled = new JCheckBox();
		this.chkStartTlsEnabled.setToolTipText(Messages.getString("panel.error.setup.reporting.smtp.tls.tooltip"));
		this.chkStartTlsEnabled.setSelected(erp.isStartTlsEnabled());
		this.chkStartTlsEnabled.setEnabled(enabled);
		
		JLabel lblUsername = new JLabel(Messages.getString("panel.error.setup.reporting.smtp.user"));
		this.txtUsername = new JTextField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// paint a watermark over the text box
				WaterMark.paintTextWaterMark(g, this, Messages.getString("panel.error.setup.reporting.smtp.user.example"));
			}
		};
		this.txtUsername.setToolTipText(Messages.getString("panel.error.setup.reporting.smtp.user.tooltip"));
		this.txtUsername.setText(erp.getAccountUsername());
		this.txtUsername.setColumns(20);
		this.txtUsername.setEnabled(enabled);
		JLabel lblPassword = new JLabel(Messages.getString("panel.error.setup.reporting.smtp.pass"));
		
		JLabel lblEmail = new JLabel(Messages.getString("panel.error.setup.reporting.smtp.email"));
		this.txtEmail = new JTextField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// paint a watermark over the text box
				WaterMark.paintTextWaterMark(g, this, Messages.getString("panel.error.setup.reporting.smtp.email.example"));
			}
		};
		this.txtEmail.setToolTipText(Messages.getString("panel.error.setup.reporting.smtp.email.tooltip"));
		this.txtEmail.setText(erp.getAccountEmail());
		this.txtEmail.setColumns(50);
		this.txtEmail.setEnabled(enabled);
		
		JPanel pnlGeneral = new JPanel();
		pnlGeneral.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		GroupLayout layout = new GroupLayout(pnlGeneral);
		pnlGeneral.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblReportingEnabled))
				.addGroup(layout.createParallelGroup()
						.addComponent(this.chkReportingEnabled)
						.addComponent(lblReporting)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblReportingEnabled)
						.addComponent(this.chkReportingEnabled))
				.addComponent(lblReporting));
		
		JPanel pnlSmtp = new JPanel();
		pnlSmtp.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		layout = new GroupLayout(pnlSmtp);
		pnlSmtp.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblAuthenticateEnabled)
						.addComponent(lblSmtpHost)
						.addComponent(lblSmtpPort)
						.addComponent(lblStartTlsEnabled)
						.addComponent(lblUsername)
						.addComponent(lblEmail))
				.addGroup(layout.createParallelGroup()
						.addComponent(this.chkAuthenticateEnabled)
						.addComponent(this.txtSmtpHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.txtSmtpPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.chkStartTlsEnabled)
						.addComponent(this.txtUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPassword)
						.addComponent(this.txtEmail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblAuthenticateEnabled)
						.addComponent(this.chkAuthenticateEnabled))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblSmtpHost)
						.addComponent(this.txtSmtpHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblSmtpPort)
						.addComponent(this.txtSmtpPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblStartTlsEnabled)
						.addComponent(this.chkStartTlsEnabled))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblUsername)
						.addComponent(this.txtUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(lblPassword)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblEmail)
						.addComponent(this.txtEmail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		
		JTabbedPane tabs1 = new JTabbedPane();
		tabs1.addTab(Messages.getString("panel.error.setup.general"), pnlGeneral);
		
		JTabbedPane tabs2 = new JTabbedPane();
		tabs2.addTab(Messages.getString("panel.error.setup.smtp"), pnlSmtp);
		
		JLabel lblMessage = new JLabel(Messages.getString("panel.error.setup.message"));
		lblMessage.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		
		layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(lblMessage)
				.addComponent(tabs1)
				.addComponent(tabs2));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(lblMessage)
				.addComponent(tabs1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(tabs2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.preferences.ui.PreferencesEditor#applyPreferences()
	 */
	@Override
	public void applyPreferences() {
		Preferences preferences = Preferences.getInstance();
		ErrorReportingPreferences erPreferences = preferences.getErrorReportingPreferences();
		
		erPreferences.setAccountEmail(this.txtEmail.getText());
		erPreferences.setAccountUsername(this.txtUsername.getText());
		erPreferences.setEnabled(this.chkReportingEnabled.isSelected());
		erPreferences.setAuthenticationEnabled(this.chkAuthenticateEnabled.isSelected());
		erPreferences.setSmtpHost(this.txtSmtpHost.getText());
		erPreferences.setSmtpPort(((Number)this.txtSmtpPort.getValue()).intValue());
		erPreferences.setStartTlsEnabled(this.chkStartTlsEnabled.isSelected());
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.chkReportingEnabled) {
			// toggle the fields
			if (this.chkReportingEnabled.isSelected()) {
				this.chkAuthenticateEnabled.setEnabled(true);
				this.txtSmtpHost.setEnabled(true);
				this.txtSmtpPort.setEnabled(true);
				this.chkStartTlsEnabled.setEnabled(true);
				this.txtUsername.setEnabled(true);
				this.txtEmail.setEnabled(true);
			} else {
				this.chkAuthenticateEnabled.setEnabled(false);
				this.txtSmtpHost.setEnabled(false);
				this.txtSmtpPort.setEnabled(false);
				this.chkStartTlsEnabled.setEnabled(false);
				this.txtUsername.setEnabled(false);
				this.txtEmail.setEnabled(false);
			}
		}
	}
}
