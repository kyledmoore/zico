package com.jitlogic.zico.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.jitlogic.zico.client.api.SystemService;
import com.jitlogic.zico.client.inject.PanelFactory;
import com.jitlogic.zico.client.resources.Resources;
import com.jitlogic.zico.client.views.hosts.HostListPanel;
import com.jitlogic.zico.shared.data.UserInfo;
import com.jitlogic.zico.widgets.client.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

public class Shell extends Composite {
    interface ShellUiBinder extends UiBinder<Widget, Shell> { }
    private static ShellUiBinder ourUiBinder = GWT.create(ShellUiBinder.class);

    @UiField(provided = true)
    HostListPanel hostListPanel;

    @UiField
    TabLayoutPanel tabPanel;

    @UiField(provided = true)
    WelcomeView welcomeView;

    @UiField
    Hyperlink lnkManageUsers;

    @UiField
    Hyperlink lnkChangePassword;

    @UiField
    Hyperlink lnkBackupConfig;

    @UiField
    Hyperlink lnkTraceTemplates;

    @UiField
    Hyperlink lnkLogout;

    @UiField(provided = true)
    StatusBar statusBar;

    private SystemService systemService;
    private PanelFactory panelFactory;

    private final String SRC = "Shell";

    private PopupMenu userMenu;
    private PopupMenu adminMenu;

    @Inject
    public Shell(final HostListPanel hostListPanel,
                 SystemService systemService, WelcomeView welcomeView,
                 PanelFactory panelFactory, MessageDisplay md) {
        this.hostListPanel = hostListPanel;
        this.welcomeView = welcomeView;
        this.systemService = systemService;
        this.panelFactory = panelFactory;
        this.statusBar = (StatusBar)md;

        initWidget(ourUiBinder.createAndBindUi(this));
        checkAdminRole();
    }


    public void addView(Widget widget, String caption) {
        tabPanel.add(widget, new CloseableTab(tabPanel, widget, caption));
        tabPanel.selectTab(widget);
    }


    private void checkAdminRole() {
        statusBar.info(SRC, "Loading user profile ...");

        systemService.user(new MethodCallback<UserInfo>() {
            @Override
            public void onFailure(Method method, Throwable e) {
                statusBar.error(SRC, "Error performing server request", e);
            }

            @Override
            public void onSuccess(Method method, UserInfo user) {
                hostListPanel.setAdminMode(user.isAdmin());
                if (!user.isAdmin()) {
                    lnkManageUsers.setVisible(false);
                    lnkBackupConfig.setVisible(false);
                    lnkTraceTemplates.setVisible(false);
                }
                statusBar.clear(SRC);

                lnkLogout.setText(user.getUserName() + " (" + user.getRealName() + ") " +
                        (user.isAdmin() ? "ADMIN" : "VIEWER"));
            }
        });
    }


    private void createUserMenu() {
        userMenu = new PopupMenu();

        userMenu.addItem(new MenuItem("Change password", Resources.INSTANCE.keyIcon(),
                new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        panelFactory.passwordChangeView("", false).asPopupWindow().show();
                    }
                }));

        userMenu.addSeparator();

        userMenu.addItem(new MenuItem("Logout", Resources.INSTANCE.ligtningGo(),
                new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        Window.Location.assign(GWT.getHostPageBaseURL() + "logout");
                    }
                }));
    }


    @UiHandler("lnkManageUsers")
    void openUserManager(ClickEvent e) {
        addView(panelFactory.userManagementPanel(), "User Management");
    }


    @UiHandler("lnkBackupConfig")
    void openUserManagementPanel(ClickEvent e) {
        systemService.backup(new MethodCallback<Void>() {
            @Override
            public void onFailure(Method method, Throwable e) {
                statusBar.error(SRC, "Error backing up symbols and configuration.", e);
            }

            @Override
            public void onSuccess(Method method, Void response) {
                Window.alert("Symbols and configuration backed up succesfully.");
            }
        });
    }


    @UiHandler("lnkTraceTemplates")
    void openTemplatePanel(ClickEvent e) {
        addView(panelFactory.traceTemplatePanel(), "Templates");
    }


    @UiHandler("lnkChangePassword")
    void changePassword(ClickEvent e) {
        panelFactory.passwordChangeView("", false).asPopupWindow().show();
    }

    @UiHandler("lnkLogout")
    void doLogout(ClickEvent e) {
        //Window.Location.assign(GWT.getHostPageBaseURL() + "logout");
        //userMenu.set
        userMenu.show();
    }

}

