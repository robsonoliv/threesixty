package za.co.yellowfire.threesixty;

import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import io.threesixty.ui.ApplicationUI;
import io.threesixty.ui.component.logo.Logo;
import io.threesixty.ui.component.notification.NotificationBuilder;
import io.threesixty.ui.event.UserPasswordChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;
import org.vaadin.spring.security.VaadinSecurity;
import org.vaadin.spring.security.util.SecurityExceptionUtils;
import org.vaadin.spring.security.util.SuccessfulLoginEvent;
import org.vaadin.spring.sidebar.components.ValoSideBar;
import org.vaadin.spring.sidebar.security.VaadinSecurityItemFilter;
import za.co.yellowfire.threesixty.domain.user.User;
import za.co.yellowfire.threesixty.ui.DashboardEvent.*;
import za.co.yellowfire.threesixty.ui.view.LoginView;
import za.co.yellowfire.threesixty.ui.view.MainView;
import za.co.yellowfire.threesixty.ui.view.security.ChangePasswordView;

@SuppressWarnings("serial")
@Theme("dashboard")
@SpringUI
@PreserveOnRefresh
public class MainUI extends ApplicationUI {
	private static final long serialVersionUID = 1L;

	//@Autowired
	//private ConverterFactory converterFactory;
    @Autowired
    private SpringViewProvider viewProvider;
    @Autowired
    private EventBus.SessionEventBus eventBus;
    @Autowired
    private ValoSideBar sideBar;
    @Autowired
    private Logo logo;
    @Autowired
    private VaadinSecurity vaadinSecurity;
    @Autowired
    private ApplicationContext applicationContext;

    private static final String TRACKER_ID = "UA-81670605-1";
    //private GoogleAnalyticsTracker tracker;

    @Override
    protected Component getSideBar() {
        sideBar.setLogo(logo);
        sideBar.setItemFilter(new VaadinSecurityItemFilter(vaadinSecurity));
        return sideBar;
    }

    @Override
    protected ViewProvider getViewProvider() {
        return viewProvider;
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.subscribe(this);
    }

    @Override
    public void detach() {
        eventBus.unsubscribe(this);
        super.detach();
    }

    @Override
    protected void init(VaadinRequest request) {
        super.init(request);

        // Let's register a custom error handler to make the 'access denied' messages a bit friendlier.
        setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                if (SecurityExceptionUtils.isAccessDeniedException(event.getThrowable())) {
                    NotificationBuilder.showNotification(
                            "Authentication",
                            "Sorry, you don't have access to do that.");
                } else {
                    super.error(event);
                }
            }
        });
        if (vaadinSecurity.isAuthenticated()) {
            showMainScreen();
        } else {
            showLoginScreen(request.getParameter("goodbye") != null);
        }
    }

    private void showMainScreen() {
        setContent(applicationContext.getBean(MainView.class));
    }

    private void showLoginScreen(boolean loggedOut) {
        LoginView loginScreen = applicationContext.getBean(LoginView.class);
        //loginScreen.setLoggedOut(loggedOut);
        setContent(loginScreen);
    }

    @EventBusListenerMethod
    void onLogin(SuccessfulLoginEvent loginEvent) {
        if (loginEvent.getSource().equals(this)) {
            access(new Runnable() {
                @Override
                public void run() {
                    showMainScreen();
                }
            });
        } else {
            // We cannot inject the Main Screen if the event was fired from another UI, since that UI's scope would be active
            // and the main screen for that UI would be injected. Instead, we just reload the page and let the init(...) method
            // do the work for us.
            getPage().reload();
        }
    }

//    @Override
//    protected void init(VaadinRequest request) {
//    	Navigator navigator = new Navigator(this, this);
//    	navigator.addProvider(viewProvider);
//    	setNavigator(navigator);
//
//    	//getSession().setConverterFactory(converterFactory);
//    	DashboardEventBus.register(this);
//
//    	Responsive.makeResponsive(this);
//        addStyleName(ValoTheme.UI_WITH_MENU);
//
//        //if (TRACKER_ID != null) {
//        //    initGATracker(TRACKER_ID, getUI().getPage().getLocation().getHost());
//        //}
//
//        updateContent();
//
//        // Some views need to be aware of browser resize events so a
//        // BrowserResizeEvent gets fired to the event bus on every occasion.
//        Page.getCurrent().addBrowserWindowResizeListener(
//                new BrowserWindowResizeListener() {
//                    @Override
//                    public void browserWindowResized(
//                            final BrowserWindowResizeEvent event) {
//                        DashboardEventBus.post(new BrowserResizeEvent());
//                    }
//                });
//    }

    /**
     * Updates the correct content for this UI based on the current user status.
     * If the user is logged in with appropriate privileges, main view is shown.
     * Otherwise login view is shown.
     */
    private void updateContent() {
        
    	User user = getCurrentUser();
        if ( user != null && !user.isPasswordChangeRequired()) {
            getNavigator().navigateTo(MainView.VIEW_NAME);
        } else if ( user != null && user.isPasswordChangeRequired()) {
        	getNavigator().navigateTo(ChangePasswordView.VIEW_NAME);
        } else {
        	getNavigator().navigateTo(LoginView.VIEW_NAME);
        }
    }
    
    private void initGATracker(final String trackerId, final String hostName) {
        //tracker = new GoogleAnalyticsTracker(trackerId, hostName);
        //tracker.extend(UI.getCurrent());
        //getNavigator().addViewChangeListener(tracker);
        //System.out.println("Tracking " + trackerId + " : " + hostName);
    }

    @Subscribe
    public void userLogin(final UserLoginEvent event) {
        VaadinSession.getCurrent().setAttribute(User.class, event.getUser());
        updateContent();
    }

    @Subscribe
    public void userPasswordChanged(final UserPasswordChangeEvent event) {
        VaadinSession.getCurrent().setAttribute(User.class, (User) event.getEntity());
        updateContent();
    }
    
    @Subscribe
    public void userLogout(final UserLogoutEvent event) {
        // When the user logs out, current VaadinSession gets closed and the
        // page gets reloaded on the login screen. Do notice the this doesn't
        // invalidate the current HttpSession.
        VaadinSession.getCurrent().close();
        Page.getCurrent().reload();
    }

    @Subscribe
    public void userProfileUpdate(final ProfileUpdatedEvent event) {
    	VaadinSession.getCurrent().setAttribute(User.class, event.getUser());
    }
    
    @Subscribe
    public void closeOpenWindows(final CloseOpenWindowsEvent event) {
        for (Window window : getWindows()) {
            window.close();
        }
    }
    
    public User getCurrentUser() {
    	return (User) VaadinSession.getCurrent().getAttribute(User.class);
    }

    //public static GoogleAnalyticsTracker getTracker() {
    //	return ((MainUI) getCurrent()).tracker;
    //}
}