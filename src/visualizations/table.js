function Table(data, elementSel, configuration, events) {
    var self = this;
    
    

    this.getTableRows = function(){
        var ret = [];
        for (len = data.length, i = 0; i < len; i++) {
            var node = data[i];
            if(!node.hasOwnProperty('children') || node.children == null || node.children.length == 0)
            {
                if(node != null){
                    parent = data[node.parent_id];
                    if(parent  != null){
                        gParent = data[parent.parent_id];
                        if(gParent != null){
                            ret.push([gParent.name,parent.name,node.name,node.properties.p,node.id]);
                        }
                    }
                }
            }
        }
        return ret;
    }


    var tableData = this.getTableRows();
    this.render = function(){

        $(elementSel).html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="tableData"></table>');

        table = $('#tableData').dataTable({
            "retrieve" : true,
            "data" : tableData,
            "columns" : [{
                "title" : "Class"
            }, {
                "title" : "Function"
            }, {
                "title" : "Line",
                "class" : "center"
            }, {
                "title" : "Error Score",
                "class" : "center"
            }],
            "order" : [3, "desc"],
        });

        table.on('click', 'tr', function() {
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
            } else {
                table.$('tr.selected').removeClass('selected');
                $(this).addClass('selected');
            }
            location.hash = "asa";
        });
    }

}