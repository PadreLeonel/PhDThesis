var ARROW_DIMENSIONS = {
  w: 195, h: 30, s: 3, t: 10
};




function initializeBreadcrumbTrail(elementSel) {
  $('#trail').remove();
  $('#endlabel').remove();
  // Add the svg area.
  var trail = d3.select(elementSel).append("svg:svg")
  .attr("width", "100%")
  .attr("height", 50)
  .attr("id", "trail");
  // Add the label at the end, for the percentage.
  d3.select(elementSel).append("div")
  .attr("id", "endlabel")}

// Generate a string that describes the points of a breadcrumb polygon.
function breadcrumbPoints(d, i) {
  var points = [];
  points.push("0,0");
  points.push(ARROW_DIMENSIONS.w + ",0");
  points.push(ARROW_DIMENSIONS.w + ARROW_DIMENSIONS.t + "," + (ARROW_DIMENSIONS.h / 2));
  points.push(ARROW_DIMENSIONS.w + "," + ARROW_DIMENSIONS.h);
  points.push("0," + ARROW_DIMENSIONS.h);
  if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
  points.push(ARROW_DIMENSIONS.t + "," + (ARROW_DIMENSIONS.h / 2));
}
return points.join(" ");
}

// Update the breadcrumb trail to show the current sequence and percentage.
function updateBreadcrumbs(nodeArray, clickFunction, configuration) {
  var last = nodeArray.slice(-1)[0];
  // Data join; key function combines name and depth (= position in sequence).
  var g = d3.select("#trail")
  .selectAll("g")
  .data(nodeArray);

  // Add breadcrumb and label for entering nodes.
  var entering = g.enter().append("svg:g")
  .on("click", clickFunction);

  entering.append("svg:polygon")
  .attr("points", breadcrumbPoints)
  .style("fill", configuration.gradiante.normal);

  entering.append("svg:text")
  .attr("x", (ARROW_DIMENSIONS.w + ARROW_DIMENSIONS.t) / 2)
  .attr("y", ARROW_DIMENSIONS.h / 2)
  .attr("dy", "0.35em")
  .attr("text-anchor", "middle")
  .text(function(d) { return d.name; });

  // Set position for entering and updating nodes.
  g.attr("transform", function(d, i) {
    return "translate(" + i * (ARROW_DIMENSIONS.w + ARROW_DIMENSIONS.s) + ", 0)";
  });

  // Remove exiting nodes.
  g.exit().remove();
  var color = configuration.gradiante.normal(last);
  // Now move and update the percentage at the end.
  $("#endlabel").html('<div class="pace pace-active" style="border-color: '+color+';"><div class="pace-progress" data-progress="'+d3.round(last.properties.p*100,2)+'" data-progress-text="'+d3.round(last.properties.p*100,2)+'%" style="width: '+d3.round(last.properties.p*100,2)+'%;   background: '+color+'; color: '+color+';"><div class="pace-progress-inner"></div></div><div class="pace-activity"></div></div>');

  // Make the breadcrumb trail visible, if it's hidden.
  d3.select("#trail")
  .style("visibility", "");

}



function NodeInfoDisplay(elementSel,clickFunction,configuration){
  var self = this;
  initializeBreadcrumbTrail(elementSel);

  this.updataBreadcumb = function(node){
    updateBreadcrumbs(getAncestors(node),clickFunction,configuration);
  }

  this.setClicked = function(node){
    self.clickedNode = node;
    self.updataBreadcumb(node);
  }

  this.setPath = function(path){
    self.path = path;
  }

  this.mouseover = function(node){
    self.updataBreadcumb(node);
    var sequenceArray = getAncestors(node);
    self.path.style("opacity", 0.3);

    self.path.filter(function(node) {
      return (sequenceArray.indexOf(node) >= 0);
    })
    .style("opacity", 1);
  }


  this.mouseleave = function(node){
    self.updataBreadcumb(self.clickedNode);
        //path.on("mouseover", null);
        self.path.style("opacity", 1);
        return;
        self.path.transition()
        .duration(1000)
        .style("opacity", 1)
        .each("end", function() {
          d3.select(this).on("mouseover", self.mouseover);
        });
      }

    }