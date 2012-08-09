package be.betty.gwtp.client.views;

import be.betty.gwtp.client.presenters.HeaderPresenter;

import com.gwtplatform.mvp.client.ViewImpl;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.widget.client.TextButton;

public class HeaderView extends ViewImpl implements HeaderPresenter.MyView {

	private final Widget widget;
	@UiField MenuBar helpMenuBar;
	@UiField MenuItem CalculeMenu;
	@UiField Label loginLabel;
	@UiField HTMLPanel contentPanel;
	@UiField MenuItem menuItemNewProject;
	@UiField TextButton buttonDisconnect;

	public interface Binder extends UiBinder<Widget, HeaderView> {
	}

	@Inject
	public HeaderView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setInSlot(Object slot, Widget content) {
		if ( slot == HeaderPresenter.SLOT_CONTENT) {
			contentPanel.clear();
			if ( content != null)
				contentPanel.add(content);
		}
		else {
			super.setInSlot(slot, content);
	
		}
	}
	
	@Override
	public MenuBar getHelpMenuBar(){
		return helpMenuBar;
	}
	
	public Label getLoginLabel(){
		return loginLabel;
	}

	@Override
	public MenuItem getCalculeMenu() {
		return CalculeMenu;
	}
	
	public MenuItem getMenuItemNewProject(){
		return menuItemNewProject;
	}
	
	public TextButton getButtonDisconnect(){
		return buttonDisconnect;
	}


}
