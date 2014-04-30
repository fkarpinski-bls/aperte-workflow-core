package org.aperteworkflow.webapi.main.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;

import java.util.*;

/**
 * Html builder for the task view
 *
 * @author mpawlak@bluesoft.net.pl
 */
public class TaskViewBuilder extends AbstractViewBuilder<TaskViewBuilder> {
    private BpmTask task;
    private List<ProcessStateAction> actions;
    private String version;
    private String description;

    @Override
    protected IAttributesProvider getViewedObject() {
        return this.task;
    }

    @Override
    protected void buildWidget(final WidgetHierarchyBean bean) {
        processWidget(bean);
    }

    @Override
    protected void buildActionButtons(final Document document) {
        addActionButtons(document);
    }

    @Override
    protected void buildAdditionalData(final Document document) {
        addVersionNumber(document);
    }

    private void addVersionNumber(Document document) {
        Element versionNumber = document.createElement("div")
                .attr("id", "versionList")
                .attr("class", "process-version");
        document.appendChild(versionNumber);

        versionNumber.append(description + " v. " + version);
    }

    /**
     * Add actions buttons compared to user privileges and process state
     */
    private void addActionButtons(Document document) {
        Element actionsNode = document.createElement("div")
                .attr("id", "actions-list")
                .attr("class", "actions-view");
        document.appendChild(actionsNode);

        Element genericActionButtons = document.createElement("div")
                .attr("id", "actions-generic-list")
                .attr("class", "btn-group  pull-left actions-generic-view");

        Element processActionButtons = document.createElement("div")
                .attr("id", "actions-process-list")
                .attr("class", "btn-group  pull-right actions-process-view");

        actionsNode.appendChild(genericActionButtons);
        actionsNode.appendChild(processActionButtons);

        /* Check if task is finished */
        if (isTaskFinished()) {
            buildCancelActionButton(genericActionButtons);
            return;
        }

        /* Check if task is from queue */
        if (isTaskHasNoOwner() && hasUserRightsToTask()) {
            addClaimActionButton(processActionButtons);
        }

        /* Check if user, who is checking the task, is the assigned person */
        if (isUserAssignedToTask() || isSubstitutingUser()) {
            buildSaveActionButton(genericActionButtons);
            for (ProcessStateAction action : actions)
                processAction(action, processActionButtons);
        }

        buildCancelActionButton(genericActionButtons);
    }


    private boolean isSubstitutingUser() {
        return ctx.getUserSubstitutionDAO().isSubstitutedBy(task.getAssignee(), user.getLogin());
    }

    private void processWidget(WidgetHierarchyBean widgetHierarchyBean) {
        IStateWidget widget = widgetHierarchyBean.getWidget();
        ProcessInstance processInstance = widgetHierarchyBean.getProcessInstance();
        Element parent = widgetHierarchyBean.getParent();

        String aliasName = widget.getClassName();

        ProcessHtmlWidget processHtmlWidget = processToolRegistry.getGuiRegistry().getHtmlWidget(aliasName);

		/* Sort widgets by prority */
        List<IStateWidget> children = new ArrayList<IStateWidget>(widget.getChildren());
        Collections.sort(children, new Comparator<IStateWidget>() {
            @Override
            public int compare(IStateWidget widget1, IStateWidget widget2) {
                return widget1.getPriority().compareTo(widget2.getPriority());
            }
        });

		/* Check if widget is based on html */
        String widgetTemplateBody = templateProvider.getTemplate(aliasName);

        if (aliasName.equals("ShadowStateWidget")) {
            IStateWidgetAttribute processStateConfigurationIdAttribute =
                    widget.getAttributeByName("processStateConfigurationId");

            IStateWidgetAttribute forcePrivilegesAttribute =
                    widget.getAttributeByName("forcePrivileges");

            Boolean forcePrivileges = Boolean.parseBoolean(forcePrivilegesAttribute.getValue());

            String attributeName = processStateConfigurationIdAttribute.getValue();
            String processStateConfigurationId = processInstance.getRootProcessInstance().getSimpleAttributeValue(attributeName);

            ProcessStateConfiguration processStateConfiguration =
                    ctx.getProcessDefinitionDAO().getCachedProcessStateConfiguration(Long.parseLong(processStateConfigurationId));


            Element divContentNode = parent.ownerDocument().createElement("div")
                    .attr("id", "vertical_layout" + widget.getId());
            parent.appendChild(divContentNode);

            for (IStateWidget childWidget : processStateConfiguration.getWidgets()) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(childWidget)
                        .setProcessInstance(processInstance.getRootProcessInstance())
                        .setForcePrivileges(forcePrivileges)
                        .setPrivileges(getPrivileges(widget));

                processWidget(childBean);
            }


        } else if (aliasName.equals("TabSheet")) {
            String tabId = "tab_sheet_" + widget.getId();
            String divContentId = "div_content_" + widget.getId();

            Element ulNode = parent.ownerDocument().createElement("ul")
                    .attr("id", tabId)
                    .attr("class", "nav nav-tabs");
            parent.appendChild(ulNode);

            Element divContentNode = parent.ownerDocument().createElement("div")
                    .attr("id", divContentId)
                    .attr("class", "tab-content");
            parent.appendChild(divContentNode);

            boolean isFirst = true;

            for (IStateWidget child : children) {
                String caption = aliasName;
                /* Set caption from attributes */
                IStateWidgetAttribute attribute = child.getAttributeByName("caption");
                if (attribute != null)
                    caption = i18Source.getMessage(attribute.getValue());

                String childId = "tab" + child.getId();

				/* Li tab element */
                Element liNode = parent.ownerDocument().createElement("li");
                ulNode.appendChild(liNode);

                Element aNode = parent.ownerDocument().createElement("a")
                        .attr("id", "tab_link_" + childId)
                        .attr("href", '#' + childId)
                        .attr("data-toggle", "tab")
                        .append(caption);

                liNode.appendChild(aNode);

                scriptBuilder.append("$('#tab_link_").append(childId).append("').on('shown', function (e) { onTabChange(e); });");

				/* Content element */
                Element divTabContentNode = parent.ownerDocument().createElement("div")
                        .attr("id", childId)
                        .attr("class", isFirst ? "tab-pane active" : "tab-pane");
                divContentNode.appendChild(divTabContentNode);

                if (isFirst)
                    isFirst = false;

                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divTabContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                processWidget(childBean);
            }

            scriptBuilder.append("$('#").append(tabId).append(" a:first').tab('show');");
        } else if (aliasName.equals("VerticalLayout")) {
            Element divContentNode = parent.ownerDocument().createElement("div")
                    .attr("id", "vertical_layout" + widget.getId());
            parent.appendChild(divContentNode);

            for (IStateWidget child : children) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                processWidget(childBean);
            }
        } else if (aliasName.equals("SwitchWidgets")) {
            List<IStateWidget> sortedList = new ArrayList<IStateWidget>(children);

            IStateWidget filteredChild = filterChildren(task, sortedList, widget);

            if (filteredChild != null) {
                Element divContentNode = parent.ownerDocument().createElement("div")
                        .attr("id", "switch_widget" + widget.getId());
                parent.appendChild(divContentNode);

                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(filteredChild)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                processWidget(childBean);
            }
        }
        /* HTML Widget */
        else if (processHtmlWidget != null) {
            Collection<String> privileges;
            if (widgetHierarchyBean.isForcePrivileges())
                privileges = widgetHierarchyBean.getPrivileges();
            else
                privileges = getPrivileges(widget);

            Map<String, Object> viewData = new HashMap<String, Object>();
            viewData.put(IHtmlTemplateProvider.PROCESS_PARAMTER, processInstance);
            viewData.put(IHtmlTemplateProvider.TASK_PARAMTER, task);
            viewData.put(IHtmlTemplateProvider.USER_PARAMTER, user);
            viewData.put(IHtmlTemplateProvider.USER_SOURCE_PARAMTER, userSource);
            viewData.put(IHtmlTemplateProvider.MESSAGE_SOURCE_PARAMETER, i18Source);
            viewData.put(IHtmlTemplateProvider.WIDGET_NAME_PARAMETER, aliasName);
            viewData.put(IHtmlTemplateProvider.PRIVILEGES_PARAMETER, privileges);
            viewData.put(IHtmlTemplateProvider.WIDGET_ID_PARAMETER, widget.getId().toString());
            viewData.put(IHtmlTemplateProvider.DICTIONARIES_DAO_PARAMETER, ctx.getProcessDictionaryDAO());
            viewData.put(IHtmlTemplateProvider.BPM_SESSION_PARAMETER, bpmSession);

            for (IStateWidgetAttribute attribute : widget.getAttributes())
                viewData.put(attribute.getName(), attribute.getValue());

            /* Add custom attributes from widget data providers */
            for (IWidgetDataProvider dataProvider : processHtmlWidget.getDataProviders())
                viewData.putAll(dataProvider.getData(task));

            String processedView = templateProvider.processTemplate(aliasName, viewData);

            Element divContentNode = parent.ownerDocument().createElement("div")
                    .append(processedView)
                    .attr("class", "html-widget-view")
                    .attr("id", "html-" + widget.getId());
            parent.appendChild(divContentNode);

            for (IStateWidget child : children) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                processWidget(childBean);
            }
        } else {
            vaadinWidgetsCount++;
            //http://localhost:8080
            String vaadinWidgetUrl = "/aperteworkflow/widget/" + task.getInternalTaskId() + "_" + widget.getId() + "/?widgetId=" + widget.getId() + "&taskId=" + task.getInternalTaskId();

            Element iFrameNode = parent.ownerDocument().createElement("iframe")
                    .attr("src", vaadinWidgetUrl)
                    .attr("autoResize", "true")
                    .attr("id", "iframe-vaadin-" + widget.getId())
                    .attr("frameborder", "0")
                    .attr("taskId", task.getInternalTaskId())
                    .attr("widgetId", widget.getId().toString())
                    .attr("class", "vaadin-widget-view")
                    .attr("widgetLoaded", "false")
                    .attr("name", widget.getId().toString());
            parent.appendChild(iFrameNode);

            scriptBuilder.append("$('#iframe-vaadin-").append(widget.getId()).append("').load(function() {onLoadIFrame($(this)); });");

            for (IStateWidget child : children) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(iFrameNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                processWidget(childBean);
            }
        }
    }

    @Override
    protected void addSpecificHtmlWidgetData(final Map<String, Object> viewData, final IAttributesProvider viewedObject) {
        viewData.put(IHtmlTemplateProvider.TASK_PARAMTER, task);
    }

    @Override
    protected Collection<String> getPrivileges(IStateWidget widget) {
        Collection<String> privileges = new ArrayList<String>();

        if (!isUserAssignedToTask() || isTaskFinished())
            return privileges;

        for (IPermission permission : widget.getPermissions()) {
            if (permission.getRoleName().contains("*") || user.hasRole(permission.getRoleName())) {
                privileges.add(permission.getPrivilegeName());
            }
        }
        return privileges;
    }

    private void processAction(ProcessStateAction action, Element parent) {

        String actionButtonId = "action-button-" + action.getBpmName();

        String actionLabel = action.getLabel();
        if (actionLabel == null)
            actionLabel = "label";
            //TODO make autohide
        else if (actionLabel.equals("hide"))
            return;

        String actionType = action.getActionType();
        if (actionType == null || actionType.isEmpty())
            actionType = "primary";

        String iconName = action.getAttributeValue(ProcessStateAction.ATTR_ICON_NAME);
        if (iconName == null || iconName.isEmpty())
            iconName = "arrow-right";

        Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", "btn btn-" + actionType)
                .attr("disabled", "true")
                .attr("type", "button")
                .attr("id", actionButtonId);
        parent.appendChild(buttonNode);


        Element actionButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-" + iconName);

        parent.appendChild(buttonNode);
        buttonNode.appendChild(actionButtonIcon);

        buttonNode.appendText(i18Source.getMessage(actionLabel));

        scriptBuilder
                .append("$('#")
                .append(actionButtonId)
                .append("').click(function() { disableButtons(); performAction(this, '")
                .append(action.getBpmName())
                .append("', ")
                .append(action.getSkipSaving())
                .append(", ")
                .append(action.getCommentNeeded())
                .append(", ")
                .append(action.getChangeOwner())
                .append(", '")
                .append(action.getChangeOwnerAttributeName())
                .append("', '")
                .append(task.getInternalTaskId())
                .append("');  });");
        scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage(action.getDescription())).append("'});");
    }

    private void addClaimActionButton(Element parent) {
        String actionButtonId = "action-button-claim";

        Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", "btn btn-warning")
                .attr("disabled", "true")
                .attr("id", actionButtonId);
        parent.appendChild(buttonNode);

        Element cancelButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-download");

        parent.appendChild(buttonNode);
        buttonNode.appendChild(cancelButtonIcon);

        buttonNode.appendText(i18Source.getMessage("button.claim"));

        Long processStateConfigurationId = task.getCurrentProcessStateConfiguration().getId();

        scriptBuilder.append("$('#").append(actionButtonId)
                .append("').click(function() { claimTaskFromQueue('#action-button-claim','null', '")
                .append(processStateConfigurationId).append("','")
                .append(task.getInternalTaskId())
                .append("'); });")
                .append("$('#").append(actionButtonId)
                .append("').tooltip({title: '").append(i18Source.getMessage("button.claim.descrition")).append("'});");
    }

    public TaskViewBuilder setActions(List<ProcessStateAction> actions) {
        this.actions = actions;
        return this;
    }

    public TaskViewBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskViewBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public TaskViewBuilder setTask(BpmTask task) {
        this.task = task;
        return this;
    }

    private boolean hasUserRightsToTask() {
        if (task.getPotentialOwners().contains(user.getLogin()))
            return true;

        for (String queueName : userQueues)
            if (task.getQueues().contains(queueName))
                return true;

        return false;
    }

    private boolean isTaskHasNoOwner() {
        return task.getAssignee() == null || task.getAssignee().isEmpty();
    }

    private boolean isTaskFinished() {
        return task.isFinished();
    }

    private boolean isUserAssignedToTask() {
        return user.getLogin().equals(task.getAssignee());
    }

    @Override
    protected TaskViewBuilder getThis() {
        return this;
    }


    @Override
    protected String getViewedObjectId() {
        return this.task.getInternalTaskId();
    }

    @Override
    protected boolean isViewedObjectClosed() {
        return isTaskFinished();
    }

    @Override
    protected String getSaveButtonMessageKey() {
        return "button.save.process.data";
    }

    @Override
    protected String getSaveButtonDescriptionKey() {
        return "button.save.process.desc";
    }

    @Override
    protected String getCancelButtonMessageKey() {
        return "button.exit";
    }

    @Override
    protected String getActionsListHtmlId() {
        return "actions-list";
    }

    @Override
    protected String getSaveButtonHtmlId() {
        return "action-button-save";
    }

    @Override
    protected String getActionsGenericListHtmlId() {
        return "actions-generic-list";
    }

    @Override
    protected String getVaadinWidgetsHtmlId() {
        return "vaadin-widgets";
    }

    @Override
    protected String getCancelButtonClickFunction() {
        return "onCancelButton();";
    }

    @Override
    protected String getCancelButtonHtmlId() {
        return "action-button-cancel";
    }
}
