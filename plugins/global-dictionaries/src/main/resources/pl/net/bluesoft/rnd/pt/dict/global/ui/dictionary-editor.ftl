<#import "/spring.ftl" as spring />
<#assign portlet=JspTaglibs["http://java.sun.com/portlet_2_0"] />

<script type="text/javascript">
	var dispatcherPortlet = '<@portlet.resourceURL id="dispatcher"/>';

</script>

<#include "apertedatatable.ftl"/>

<style type="text/css">
    #fixedElement.stick {
        position: fixed;
        top: 0;
        z-index: 1;
        background-color: white;
    }

    table {
        border:none;
        border-collapse: collapse;
    }

    table tr {
        border-top: 1px solid #ddd;
        border-bottom: 1px solid #ddd;
    }

    table tr:first-child {
        border-top: none;
        border-bottom: none;
    }

    table tr:last-child {
        border-bottom: none;
    }

    input[type="text"] {
        color: #000 !important;
    }

    .input-hidden {
        position: absolute;
        left: -9999px;
    }

    input[type=radio]:checked + label>img {
        border: 1px solid #fff;
        box-shadow: 0 0 1px 1px #090;
    }

    input[type=radio] + label>img {
        border: 1px solid #fff;
        transition: 300ms all;
    }
</style>

<#if aperteUser.hasRole("DICT_EDITOR")>

<div class="apw main-view col-md-offset-1">
    <div class="panel panel-default">
        <div class="panel-heading">
            <@spring.message "dictionary.editor.heading"/>
            <div class="pull-right">
                <div class="col-md-4">
                    <label for="dictName"><@spring.message "dictionary.editor.dictionaryName"/></label>
                </div>
                <div class="col-md-4">
                    <input id="dictName" type="text" placeholder="" style="width:280px"/>
                </div>
            </div>
        </div>
        <div class="panel-body">
            <div id="itemsList">
                <div class="row">
                    <div class="col-md-8">
                        <button type="button" id="addNew" class="btn btn-primary btn-xs"><@spring.message "dictionary.editor.dictionaryItems.button.addNew"/></button>
                    </div>
                </div>
                <br>

                <div class="panel-body">
                    <table cellpadding="0" cellspacing="0" border="0" id="itemsTable"
                           class="table table-hover table-bordered" style="width:100%;">
                        <thead>
                            <th style="font-size: 11px!important; width:110px"><@spring.message "dictionary.editor.dictionaryItems.table.key"/></th>
                            <th style="font-size: 11px!important;width:50px"><@spring.message "dictionary.editor.dictionaryItems.table.description"/></th>
                            <th style="font-size: 11px!important;width:210px"><@spring.message "dictionary.editor.dictionaryItems.table.actions"/></th>
                        </thead>
                        <tbody style="font-size: 12px!important;vertical-align:middle;">

                        </tbody>
                    </table>
                </div>
            </div>
            <div id="itemsEdit" hidden>
                <div class="row">
                    <div class="col-md-2">
                        <button type="button" id="saveButton" class="btn btn-success btn-xs"><@spring.message "dictionary.editor.dictionaryItems.button.save"/></button>
                        <button type="button" id="backButton" class="btn btn-warning btn-xs"><@spring.message "dictionary.editor.dictionaryItems.button.cancel"/></button>
                    </div>
                </div>
                <br>

                <div class="row">
                    <div class="form-group col-md-4">
                        <label for="description" class="col-sm-2 control-label"><@spring.message "dictionary.editor.dictionaryItems.table.key"/></label>

                        <div class="col-sm-10">
                            <input class="form-control" id="itemKey" placeholder="<@spring.message 'dictionary.editor.dictionaryItems.table.key'/>"></textarea>
                        </div>
                    </div>
                    <div class="form-group col-md-4">
                        <label for="description" class="col-sm-2 control-label"><@spring.message "dictionary.editor.dictionaryItems.table.description"/></label>

                        <div class="col-sm-10" id="itemDescDiv">
                            <input class="form-control" id="itemDesc" placeholder="<@spring.message 'dictionary.editor.dictionaryItems.table.description'/>"></textarea>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-2 pull-right">
                        <button type="button" id="addNewValue" class="btn btn-primary btn-xs"><@spring.message 'dictionary.editor.dictionaryItems.button.addValue'/></button>
                    </div>
                </div>
                <div class="row">
                    <div class="panel-body">
                        <table cellpadding="0" cellspacing="0" border="0" id="valuesTable"
                               class="table table-hover table-bordered" style="width:100%;">
                            <thead>
                            <tr>
                                <th style="font-size: 11px!important; width:110px"><@spring.message 'dictionary.editor.itemValues.table.value'/></th>
                                <th style="font-size: 11px!important;width:50px"><@spring.message 'dictionary.editor.itemValues.table.dateFrom'/></th>
                                <th style="font-size: 11px!important;width:50px"><@spring.message 'dictionary.editor.itemValues.table.dateTo'/></th>
                                <th style="font-size: 11px!important;width:410px"><@spring.message 'dictionary.editor.itemValues.table.extensions'/></th>
                                <th style="font-size: 11px!important;width:80px"><@spring.message 'dictionary.editor.itemValues.table.actions'/></th>
                                <th hidden />
                            </tr>

                            </thead>
                            <tbody style="font-size: 12px!important;vertical-align:middle;">

                            </tbody>
                        </table>

                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade aperte-modal" id="alertModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">


        <div class="panel panel-warning">
            <div class="panel-heading"><h4><@spring.message code="processes.alerts.modal.title" /></h4></div>
            <ul id="alerts-list">

            </ul>
            <button type="button" class="btn btn-warning" data-dismiss="modal" style="margin: 20px;"><@spring.message code="processes.alerts.modal.close" /></button>
        </div>

    </div><!-- /.modal-dialog -->
</div>

<script type="text/javascript">
//<![CDATA[

    var itemsTable;
    var valuesTable;
    var currentDict;
    var currentItem = {"key":"", "description":"", "selectedLanguage":"default", "localizedDescriptions": {"default": {"languageCode":"default", "text":""}}};

    var alertsShown = false;
    var alertsInit = false;

    $(document).ready(function () {
        $('[name="tooltip"]').tooltip();

        valuesTable = $('#valuesTable').dataTable({
            "bPaginate": true,
            "bLengthChange": false,
            "bFilter": false,
            "bSort": true,
            "bInfo": false,
            "bAutoWidth": false,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 25, 50, 100],[10, 25, 50, 100]],
            "oLanguage": {
                "sEmptyTable": "<@spring.message 'datatable.empty' />",
                "sInfoEmpty": "<@spring.message 'datatable.empty' />",
                "sProcessing": "<@spring.message 'datatable.processing' />",
                "sLengthMenu": "<@spring.message 'datatable.records' />",
                "sInfoFiltered": "",
                "oPaginate": {
                "sFirst": "<@spring.message 'datatable.paginate.firstpage' />",
                "sNext": "<@spring.message 'datatable.paginate.next' />",
                "sPrevious": "<@spring.message 'datatable.paginate.previous' />"
                }
            },
            "aoColumns":[
                 { "sName":"value", "bSortable": false ,"mData": function(o) { return ""; }, "fnCreatedCell": function(nTd, sData, oData, iRow, iCol) { return generateValueColumn(nTd, sData, oData, iRow, iCol) }
                 },
                 { "sName":"dateFrom", "bSortable": false ,"mData": function(o) { return ""; }, "fnCreatedCell": function(nTd, sData, oData, iRow, iCol) { return generateValueDateFromColumn(nTd, sData, oData, iRow, iCol) }
                 },
                 { "sName":"dateTo", "bSortable": false ,"mData": function(o) { return ""; }, "fnCreatedCell": function(nTd, sData, oData, iRow, iCol) { return generateValueDateToColumn(nTd, sData, oData, iRow, iCol) }
                 },
                 { "sName":"extensions", "bSortable": false ,"mData": function(o) { return ""; }, "fnCreatedCell": function(nTd, sData, oData, iRow, iCol) { return generateValueExtensionsColumn(nTd, sData, oData, iRow, iCol) }
                 },
                 { "sName":"actions", "bSortable": false , "mData": function(o) { return ""; }, "fnCreatedCell": function(nTd, sData, oData, iRow, iCol) { return generateValueActionsColumn(nTd, sData, oData, iRow, iCol) }
                 },
                 { "sName":"toDelete", "bSortable": false , "mData": "toDelete"
                 }
            ],
            "fnRowCallback" : function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                if (aData.toDelete == true)
                    $(nRow).attr("hidden", true);
                return nRow;
            }
        });
        valuesTable.fnSetColumnVis(5, false);

        function generateValueColumn(nTd, sData, oData, iRow, iCol){
            var valueControl = $(
                '<input style="width:200px" type="text" class="form-control" id="value-' + iRow + '" placeholder="<@spring.message 'dictionary.editor.itemValues.table.value'/>">'
            );
            //valueControl.val(oData.value);
            var selectedLanguage = currentItem.values[iRow].selectedLanguage;
            var value = currentItem.values[iRow].localizedValues[selectedLanguage];
            if (selectedLanguage && value)
                valueControl.val(value.text);
            else if (selectedLanguage && !value)
                valueControl.val('');
            else
                valueControl.val(currentItem.values[iRow].value);
            $(valueControl).on('change',function(){
                currentItem.values[iRow].value = $( this ).val() ;
                var lang = $('input:radio[name=languageSelector-'+iRow+']:checked').val();
                if (!currentItem.values[iRow].localizedValues[lang])
                    currentItem.values[iRow].localizedValues[lang] = {"languageCode":lang, "text":""};
                currentItem.values[iRow].localizedValues[lang].text = $(this).val();
            });
            var selector = languageSelector(iRow, currentItem.values[iRow]);
            $(selector).on('change',function(){
                var iId = 'languageSelector-' + iRow;
                var lang = $('input:radio[name='+iId+']:checked').val();
                currentItem.values[iRow].selectedLanguage = lang;
                var localizedValue = currentItem.values[iRow].localizedValues[lang];
                if (!localizedValue) {
                    $('#value-' + iRow).val('');
                } else {
                    $('#value-' + iRow).val(localizedValue.text);
                }
            });
            $(nTd).prepend(selector);
            $(nTd).prepend(valueControl);
        }

        function languageSelector(iRow, oValue) {
            var iId = 'languageSelector-' + iRow;
            var languages = ${languages};
            var html = '<div id="' + iId + '">';
            $.each(Object.keys(languages), function(index) {
                html += '<input class="input-hidden" type="radio" id="' + iId + '-' + index+ '" name="' + iId + '" value="' + this +'" ';
                if ((!oValue.selectedLanguage && index==0) || oValue.selectedLanguage == this)
                    html += 'checked';
                html += '/>';
                html += '<label for="' + iId+'-'+index + '"><img src="/html/themes/control_panel/images/language/' + languages[this] + '.png"/>';
                html += '</label>';
            });
            html += '</div>';
            var selector = $(html);
            return selector;
        }

        // add language selector to the item description input
        var itemDescLangSelector = languageSelector('itemDesc', currentItem);
        $('#itemDescDiv').append(itemDescLangSelector);
        $(itemDescLangSelector).on('change',function(){
            var sId = 'languageSelector-itemDesc';
            var lang = $('input:radio[name='+sId+']:checked').val();
            currentItem.selectedLanguage = lang;
            var localizedDesc = currentItem.localizedDescriptions[lang];
            if (!localizedDesc) {
                $('#itemDesc').val('');
            } else {
                $('#itemDesc').val(localizedDesc.text);
            }
        });
        $('#itemDesc').on('change',function(){
            currentItem.description = $(this).val();
            var lang = $('input:radio[name=languageSelector-itemDesc]:checked').val();
            if (!currentItem.localizedDescriptions[lang])
                currentItem.localizedDescriptions[lang] = {"languageCode":lang, "text":""};
            currentItem.localizedDescriptions[lang].text = $(this).val();
        });

        function generateValueDateFromColumn(nTd, sData, oData, iRow, iCol){
            var dataControl = $('<div class="input-group date" style="width:150px"><input type="text" class="form-control datepicker" id="valueDateFrom" placeholder="<@spring.message 'dictionary.editor.itemValues.table.dateFrom'/>"><span class="input-group-addon"><i class="glyphicon glyphicon-th"></i></span></div>');
            $(dataControl).datepicker({
                "format": "yyyy-mm-dd",
                autoclose: true
            });
            $(dataControl).datepicker('update', currentItem.values[iRow].dateFrom);
            $(dataControl).on('changeDate',function(e){
                var dateString = $.format.date(e.dates[0], "yyyy-MM-dd");
                currentItem.values[iRow].dateFrom = dateString;
            });
            $(nTd).prepend(dataControl);
        }

        function generateValueDateToColumn(nTd, sData, oData, iRow, iCol){
            var dataControl = $('<div class="input-group date" style="width:150px"><input type="text" class="form-control datepicker" id="valueDateTo" placeholder="<@spring.message 'dictionary.editor.itemValues.table.dateTo'/>"><span class="input-group-addon"><i class="glyphicon glyphicon-th"></i></span></div>');
            $(dataControl).datepicker({
                "format": "yyyy-mm-dd",
                autoclose: true
            });
            $(dataControl).datepicker('update', currentItem.values[iRow].dateTo);
            $(dataControl).on('changeDate',function(e){
                var dateString = $.format.date(e.dates[0], "yyyy-MM-dd");
                currentItem.values[iRow].dateTo = dateString;
            });
            $(nTd).prepend(dataControl);
        }

        function generateValueActionsColumn(nTd, sData, oData, iRow, iCol) {
            var removeButton = $('<button type="button" class="btn btn-danger btn-xs"><@spring.message 'dictionary.editor.itemValues.button.delete'/></button>');
            removeButton.button();
            removeButton.on('click',function(){
                removeValue(oData, iRow);
            });
            $(nTd).prepend("&nbsp;");
            $(nTd).prepend(removeButton);
        }

        function generateValueExtensionsColumn(nTd, sData, oData, iRow, iCol) {
            $(nTd).empty();
            var row = $('<div class="col-md-12" ></div>');
            $.each(oData.extensions, function(index) {
                if (!this.toDelete) {
                    var innerRow = $('<div class="row"></div>');
                    var lineKey = $('<div class="form-group col-md-5"><label for="description" class="control-label"><@spring.message "dictionary.editor.valueExtensions.table.key"/></label>' +
                                    '<div><input type="text" class="form-control" value="'+this.key+'" placeholder="<@spring.message "dictionary.editor.valueExtensions.table.key"/>"></input></div></div>');

                    $(lineKey).find('input').on('change',function(){
                        currentItem.values[iRow].extensions[index].key = $( this ).val() ;
                    });

                    var lineValue = $('<div class="form-group col-md-5"><label for="description" class="control-label"><@spring.message "dictionary.editor.valueExtensions.table.value"/></label>' +
                                    '<div><input type="text" class="form-control" value="'+this.value+'" placeholder="<@spring.message "dictionary.editor.valueExtensions.table.value"/>"></input></div></div>');

                    $(lineValue).find('input').on('change',function(){
                        currentItem.values[iRow].extensions[index].value = $( this ).val() ;
                    });

                    var lineButtonDiv = $('<div class="form-group col-md-2"></div>');

                    var removeButton = $('<span name="tooltip" title="<@spring.message 'dictionary.editor.valueExtensions.button.delete'/>" class="glyphicon glyphicon-trash" style="font-size: 16px!important;cursor:pointer;margin-top:25px"></span>');

                    removeButton.on('click',function(){
                        removeExtension(iRow, index);
                    });

                    $(lineButtonDiv).append(removeButton);

                    $(innerRow).append(lineKey);
                    $(innerRow).append(lineValue);
                    $(innerRow).append(lineButtonDiv);

                    $(row).append(innerRow);
                }
            });
            $(nTd).append(row);

            var addExtensionButton = $('<button type="button" class="btn btn-info btn-xs" style="margin: 10px;"><@spring.message "dictionary.editor.itemValues.button.addExtension"/></button>');
                addExtensionButton.button();
                addExtensionButton.on('click',function(){
                    addNewExtension(iRow);
                });
            $(nTd).append("&nbsp;");
            $(nTd).append(addExtensionButton);
        }

        itemsTable = new AperteDataTable("itemsTable",
            [
                 { "sName":"key", "bSortable": true ,"mData": "key" },
                 { "sName":"description", "bSortable": false ,"mData": "description" },
                 { "sName":"actions", "bSortable": false , "mData": function(o) { return ""; }, "fnCreatedCell": function(nTd, sData, oData, iRow, iCol) { return generateActionsColumn(nTd, sData, oData, iRow, iCol) }
                 }
            ],
            [[ 0, "asc" ]]
        );

        function generateActionsColumn(nTd, sData, oData, iRow, iCol) {
            var editButton = $('<button type="button" class="btn btn-primary btn-xs"><@spring.message "dictionary.editor.dictionaryItems.button.edit"/></button>');
            editButton.button();
            editButton.on('click',function(){
                edit(oData);
            });
            $(nTd).empty();
            $(nTd).prepend(editButton);

            var removeButton = $('<button type="button" class="btn btn-danger btn-xs"><@spring.message "dictionary.editor.dictionaryItems.button.delete"/></button>');
            removeButton.button();
            removeButton.on('click',function(){
                remove(oData);
            });
            $(nTd).prepend("&nbsp;");
            $(nTd).prepend(removeButton);
        }

        itemsTable.addParameter("controller", "dictionaryeditorcontroller");
        itemsTable.addParameter("action", "getDictionaryItems");
        itemsTable.reloadTable(dispatcherPortlet);

        $('#dictName').select2({
            ajax: {
                url: dispatcherPortlet,
                dataType: 'json',
                quietMillis: 0,
                data: function (term, page) {
                    return {
                        q: term, // search term
                        page_limit: 10,
                        controller: "dictionaryeditorcontroller",
                        page: page,
                        action: "getAllDictionaries"
                    };
                },
                results: function (data, page)
                {
                    var results = [];
                    $.each(data.data, function(index, item) {
                        results.push({
                            id: item.id,
                            text: item.name
                        });
                    });
                    return {
                        results: results
                    };
                },
                initSelection: function(element, callback) {
                    callback('');
                }
            }
        });
		$('#dictName').on('change', function(){
			currentDict = $('#dictName').val();
			refreshTable();
		});
		$("#backButton").on('click',function() {
			$("#itemsList").fadeIn(300);
			$("#itemsEdit").hide();
            valuesTable.fnClearTable();
			refreshTable();
		});

		$("#addNew").on('click',function() {
			addNew();
		});

		$("#addNewValue").on('click',function()
		{
			addNewValue();
		});

		$("#saveButton").on('click',function() {
		    currentItem.key = $("#itemKey").val();
		    currentItem.description = $("#itemDesc").val();
            var widgetJson = $.post(dispatcherPortlet, {
                "controller": "dictionaryeditorcontroller",
                "action": "saveDictionaryItem",
                "item": JSON.stringify(currentItem, null, 2),
                "dictId": currentDict
            })
            .done(function(data) {
                <!-- Errors handling -->
                clearAlerts();
                var errors = [];
                $.each(data.errors, function() {
                    errors.push(this);
                    addAlert(this.message);
                });
                if(errors.length > 0) { return; }

			    $("#itemsEdit").hide();
			    refreshTable();
			    $("#itemsList").fadeIn(300);
            });
		});

    });

	function addNewExtension(iRow) {
		var newExtension = {"key":"", "value":"", "toDelete":false};
		var extensions = currentItem.values[iRow].extensions;

		currentItem.values[iRow].extensions[extensions.length] = newExtension;

		valuesTable.fnClearTable();
		valuesTable.fnAddData(currentItem.values);
	}

	function removeExtension(iRow, index) {
	    if (currentItem.values[iRow].extensions[index].id)
	        currentItem.values[iRow].extensions[index].toDelete = true;
	    else
		    currentItem.values[iRow].extensions.splice(index, 1);

		valuesTable.fnClearTable();
		valuesTable.fnAddData(currentItem.values);
	}

	function removeValue(value, iRow) {
	    if (!value.id)
		    currentItem.values.splice(iRow, 1);
		else
		    currentItem.values[iRow].toDelete = true;

		valuesTable.fnClearTable();
        valuesTable.fnAddData(currentItem.values);
	}

	function addNew() {
	    if (!currentDict)
	        return;
		edit({"key":"", "description": "", "values":[], "localizedDescriptions": []});
	}

	function addNewValue() {
		var row = {"value": "", "dateFrom": "", "dateTo":"", "extensions":[], "toDelete":false, "localizedValues":{"default":{"languageCode":"default","text":""}} };
		currentItem.values.push( row );
        valuesTable.fnAddData(row);
	}

	function edit(item) {
        $("#itemsList").hide();
        currentItem = item;
        currentItem.selectedLanguage = "default";
        $('input:radio[name=languageSelector-itemDesc][value=default]').prop('checked', true);
        refreshValuesTable();
        $("#itemKey").val(item.key);
        $("#itemDesc").val(item.description);
        $("#itemsEdit").fadeIn(300);
    }

	function remove(item) {
	    if (confirm('<@spring.message "dictionary.editor.dictionaryItems.confirm.delete"/>')) {
            var widgetJson = $.post(dispatcherPortlet, {
                "controller": "dictionaryeditorcontroller",
                "action": "deleteDictionaryItem",
                "item": JSON.stringify(item, null, 2),
                "dictId": currentDict
            })
            .done(function(data) {
                <!-- Errors handling -->
                clearAlerts();
                var errors = [];
                $.each(data.errors, function() {
                    errors.push(this);
                    addAlert(this.message);
                });
                if(errors.length > 0) { return; }

                refreshTable();
            });
        }
    }

	function refreshTable() {
		var url = dispatcherPortlet;
        url += "&dictId=" + encodeURI(currentDict.replace(/#/g, '%23'));
        // itemsTable.addParameter("dictId", currentDict);
		itemsTable.reloadTable(url);
	}

	function refreshValuesTable() {
	    // get values from server
		var widgetJson = $.post(dispatcherPortlet, {
                "controller": "dictionaryeditorcontroller",
                "action": "getItemValues",
                "itemId": currentItem.id,
                "dictId": currentDict
        })
        .done(function(data) {
            <!-- Errors handling -->
            clearAlerts();
            var errors = [];
            $.each(data.errors, function() {
                errors.push(this);
                addAlert(this.message);
            });
            if(errors.length > 0) { return; }

            var values = data.data;
            currentItem.values = values;
            valuesTable.fnClearTable();
            valuesTable.fnAddData(values);
        });
	}


    function isOdd(n) {
       return isNumber(n) && (Math.abs(n) % 2 == 1);
    }

    function isNumber(n) {
       return n === parseFloat(n);
    }

    addAlert = function(alertMessage) {
		if(alertsShown == false) {
			if(alertsInit == false) {
				alertsInit = true;
				$('#alertModal').appendTo("body").modal({
				    keyboard: false
				});
				$('#alertModal').on('hidden.bs.modal', function (e) {
					clearAlerts();
					alertsShown = false;
				});

			} else {
				$('#alertModal').appendTo("body").modal('show');
			}
			alertsShown = true;
		}
		$('#alerts-list').append('<li><h5>'+alertMessage+'</h5></li>');
	}

	clearAlerts = function() {
		$('#alerts-list').empty();
	}

	addAlerts = function(alertsMessages) {
		clearAlerts();
		$.each(alertsMessages, function() {
			addAlert(this.message);
		});
	}


//]]>

</script>

<#else>

    <div class="apw main-view col-md-offset-1">
        <div class="panel panel-default">
            <div class="panel-heading">
                <@spring.message "dictionary.editor.access.denied"/>
            </div>
        </div>
    </div>

</#if>