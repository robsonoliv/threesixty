package za.co.yellowfire.threesixty.ui.view.org;

import com.vaadin.server.Resource;
import com.vaadin.ui.Layout;
import za.co.yellowfire.threesixty.domain.organization.Organization;

import java.util.List;
import java.util.stream.Collectors;

public class OrganizationModel {

	private final Organization organization;
	private OrganizationIconResolver iconResolver;
	private Layout form;
	
	OrganizationModel(final Organization organization, final OrganizationIconResolver iconResolver) {
		this.organization = organization;
		this.iconResolver = iconResolver;
	}
	
	public void setForm(final Layout form) {
		this.form = form;
	}
	
	public Layout getForm() {
		return this.form;
	}
	
	public String getName() {
		return organization.getName();
	}
	
	boolean hasChildren() {
		return organization.hasChildren();
	}
	
	public Organization getOrganization() {
		return organization;
	}

	List<OrganizationModel> getChildren() {
		return organization.getChildren()
				.stream()
				.map(o -> new OrganizationModel(o, this.iconResolver))
				.collect(Collectors.toList());
	}
	
	public Resource getIcon() {
		return iconResolver.getIcon(organization.getMetadata().orElse(null));
	}
	
	public String toString() {
		return this.organization.getName();
	}
}
