package za.co.yellowfire.threesixty.ui.component.notification;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import org.apache.commons.lang3.StringUtils;
import za.co.yellowfire.threesixty.domain.user.UserService;
import za.co.yellowfire.threesixty.ui.component.ButtonBuilder;
import za.co.yellowfire.threesixty.ui.component.button.HeaderButtons;

public class NotificationsHeaderButtons extends HeaderButtons {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * @param editButtonId The id of the edit button. If null then the button is not added to the header
	 */
	public NotificationsHeaderButtons(final UserService userService, final String editButtonId, final Component...components) {
		super(components);
		
		addComponent(NotificationsButton.BELL(userService, this::onViewNotifications));
		if (StringUtils.isNotBlank(editButtonId)) {
			addComponent(ButtonBuilder.DASHBOARD_EDIT(editButtonId, this::onDashBoardEdit));
		}
	}
	
	private void onDashBoardEdit(final ClickEvent event) {
//          getUI().addWindow(
//                  new DashboardEdit(DashboardView.this, titleLabel
//                          .getValue()));
	}
	
	public void onViewNotifications(final ClickEvent event) {
    	Notification.show("Not implemented in this demo");
    }
}
