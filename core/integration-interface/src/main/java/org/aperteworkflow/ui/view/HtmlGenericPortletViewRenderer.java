package org.aperteworkflow.ui.view;

import pl.net.bluesoft.util.lang.cquery.CQuery;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.List;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-04-04
 */
public abstract class HtmlGenericPortletViewRenderer implements GenericPortletViewRenderer {
	private final String key;
	private final String name;
	private final int position;

	private final HtmlPortletTemplateLoader templateLoader;

	private static final String TEMPLATE_NAME = "myTemplate";

	protected HtmlGenericPortletViewRenderer(String key, String name, int position, String template) {
		this.key = key;
		this.name = name;
		this.position = position;

		this.templateLoader = new HtmlPortletTemplateLoader();
		this.templateLoader.addTemplate(TEMPLATE_NAME, template);


	}

    private List<GenericPortletViewRenderer> arrangeViews(List<GenericPortletViewRenderer> permittedViews) {
        return CQuery.from(permittedViews).orderBy(new F<GenericPortletViewRenderer, Comparable>() {
            @Override
            public Comparable invoke(GenericPortletViewRenderer x) {
                return x.getPosition();
            }
        }).toList();
    }

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String[] getRequiredRoles() {
		return new String[] {};
	}

	@Override
	public Object render(RenderParams params) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String getCode() {
		Map<String, Object> templateData = getTemplateData();
		return templateLoader.processTemplate(TEMPLATE_NAME, templateData);
	}

	protected abstract Map<String, Object> getTemplateData();
}