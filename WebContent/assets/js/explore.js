var entity_type = 1;
var relation_type = 2;
var concept_type = 3;
var count = 0;

function getResult(query, type){
	var url = "http://47.88.28.16:8080/Movie/Explore?" + query + "type=" + type;
	console.log(url);
	return $.ajax({
    	url: url,
		type: "GET",
		contentType: "application/json; charset=utf-8",
		async: false,
		dataType: "jsonp",
		jsonpCallback: "callback"
	});
}

function getQuery(){
	var entities = document.getElementsByName("entity");
	var query = "";
	for(var i = 0; i < entities.length; i ++)
		if(entities[i].value != "")
			query += "entity=" + entities[i].value + "&";
	
	var features = document.getElementsByName("feature");
	for(var i = 0; i < features.length; i ++)
		if(features[i].checked == true)
			query += "feature=" + features[i].value + "&";
	
	return query;
}

function checkKey(event){
	if(event.keyCode == 13)
		explore();
}

function explore(){
	var query = getQuery();
	if(query != "")
		createResultBox(query);
	else
		console.log("no input");
}

function flashInput(result){
	var inputBox = document.getElementById("inputBox");
	var attach = document.getElementById("attach");
	while(inputBox.hasChildNodes() && inputBox.firstChild.id != "attach")
		inputBox.removeChild(inputBox.firstChild);
	
	for(var i = 0; i < result.queryEntityList.length; i ++)
		if(result.queryEntityList[i].type != relation_type)
			inputBox.insertBefore(createSpan_input(result.queryEntityList[i].name, result.queryEntityList[i].type), attach);
	
	attach.getElementsByTagName("input")[0].value = "";
}

function removeResultBox(id){
	var object = document.getElementById(id);
	if(object != null)
		object.parentNode.removeChild(object);
}

function createResultBox(query) {
	getResult(query, 0).success(function callback(data){
		var result = eval('(' + data + ')'); 
		console.log(result);
		if(result != null){		
			console.log(result);
			var resultGroup = document.getElementById("resultGroup");
			while(resultGroup.firstChild)
				resultGroup.removeChild(resultGroup.firstChild);
			
			var resultBox = document.createElement("div");
			var id = "result" + (count ++);
			resultBox.setAttribute("id", id);
			resultBox.setAttribute("class", "col-md-12");
			resultBox.setAttribute("style", "margin: 0px; height: 100%;");
					
			resultGroup.appendChild(resultBox);
			createResult(id, result);	
		}
	})
}

function createResult(id, result){
	var resultBox = document.getElementById(id);
	
	//flash query
	flashInput(result[0]);
	
	//create result
	resultBox.appendChild(createQueryEntityBox(result[0]));
	resultBox.appendChild(createFeatureBox(result[0]));
	resultBox.appendChild(createResultEntityBox(result[0]));
}

function createQueryEntityBox(result){
	var queryEntityBox = document.createElement("div");
	queryEntityBox.setAttribute("id", "queryEntityBox");
	queryEntityBox.setAttribute("class", "col-md-3");
	queryEntityBox.setAttribute("style", "height: 600px");
	queryEntityBox.setAttribute("ondrop", "addSpan(event)");
	queryEntityBox.setAttribute("ondragover", "allowDrop(event)");
	
	for(var i = 0; i < result.queryEntityList.length; i ++)
		queryEntityBox.appendChild(createEntityBox(result.queryEntityList[i], 2));
	
	return queryEntityBox;
}

function createFeatureBox(result){
	var featureBox = document.createElement("div");
	featureBox.setAttribute("class", "col-md-6");
	featureBox.setAttribute("style", "height: 600px");
	featureBox.setAttribute("id", "featureBox");
	
	var queryFeatureSet = new Set();
	for(var i = 0; i < result.queryFeatureList.length; i ++)
		queryFeatureSet.add(result.queryFeatureList[i].target.name + "_" + result.queryFeatureList[i].relation.name + "_" + result.queryFeatureList[i].relation.direction);
	
	var relationSet = new Set();
	for(var i = 0; i < result.resultFeatureList.length; i ++){	
		var entityString = "";
		for(var j = 0; j < result.resultFeatureList[i].entitySet.length; j ++){
			if(j != result.resultFeatureList[i].entitySet.length - 1)	
				entityString += result.resultFeatureList[i].entitySet[j].name + "_";
			else
				entityString += result.resultFeatureList[i].entitySet[j].name;
		}
		entityString = entityString.replace("\'", "\\\'");
		
		var featureGroup = document.createElement("div");
		featureGroup.setAttribute("class", "row");
		featureGroup.setAttribute("style", "margin: 10px; ");
		featureGroup.setAttribute("onmouseover", "match('" + entityString + "')");
		featureGroup.setAttribute("id", result.resultFeatureList[i].relation.name);
		
		var input = document.createElement("input");
		input.setAttribute("name", "feature");
		var featureString = result.resultFeatureList[i].target.name + "_" + result.resultFeatureList[i].relation.name + "_" + result.resultFeatureList[i].relation.direction;
		input.setAttribute("value", featureString);
		input.setAttribute("type", "checkbox");
		if(queryFeatureSet.has(featureString))
			input.setAttribute("checked", "true");
		input.setAttribute("style", "margin: 5px;float: left");
		input.setAttribute("onclick", "explore()");
				
		featureGroup.appendChild(input);
		featureGroup.appendChild(createSpan_relation(result.resultFeatureList[i].relation));
		featureGroup.appendChild(createSpan_entity(result.resultFeatureList[i].target));
		
		var a = document.createElement("a");
		a.innerHTML = "more";
		a.setAttribute("style", "float: right");
		a.setAttribute("onclick", "more('" + result.resultFeatureList[i].relation.name + "', this)");
	
		if(relationSet.has(result.resultFeatureList[i].relation.name))
			featureGroup.setAttribute("style", "display: none");
		else{
			var hr = document.createElement("hr");
			featureBox.appendChild(hr);
			featureGroup.appendChild(a);
		}
		
		relationSet.add(result.resultFeatureList[i].relation.name);
		featureBox.appendChild(featureGroup);
	}
	
	return featureBox;
}

function more(relation, object){
	object.innerHTML = "less";
	object.setAttribute("onclick", "less('" + relation + "', this)");
	
	var featureGroups = document.getElementById("featureBox").children;
	for(var i = 0; i < featureGroups.length; i ++){
		if(featureGroups[i].id == relation)
			featureGroups[i].setAttribute("style", "margin: 10px");
	}
}

function less(relation, object){
	object.innerHTML = "more";
	object.setAttribute("onclick", "more('" + relation + "', this)");
	
	var featureGroups = document.getElementById("featureBox").children;
	var flag = false;
	for(var i = 0; i < featureGroups.length; i ++){
		if(featureGroups[i].id == relation){
			if(flag)
				featureGroups[i].setAttribute("style", "display: none");
			else
				flag = true;
		}
	}
}

function match(entityString){
	entityString = entityString.replace("\\\'", "\'");
	var entities = entityString.split("_");
	var entitySet = new Set();
	for(var i = 0; i < entities.length; i ++)
		entitySet.add(entities[i]);
	
	var queryEntityBox = document.getElementById("queryEntityBox");
	for(var i = 0; i < queryEntityBox.childNodes.length; i ++){
		queryEntityBox.childNodes[i].setAttribute("style", queryEntityBox.childNodes[i].getAttribute("style").replace(";border-left: 6px solid red;", ""));
		if(entitySet.has(queryEntityBox.childNodes[i].id))
			queryEntityBox.childNodes[i].setAttribute("style", queryEntityBox.childNodes[i].getAttribute("style") + ";border-left: 6px solid red;");
	}
	
	var resultEntityBox = document.getElementById("resultEntityBox");
	for(var i = 0; i < resultEntityBox.childNodes.length; i ++){
		resultEntityBox.childNodes[i].setAttribute("style", resultEntityBox.childNodes[i].getAttribute("style").replace(";border-left: 6px solid red;", ""));
		if(entitySet.has(resultEntityBox.childNodes[i].id))
			resultEntityBox.childNodes[i].setAttribute("style", resultEntityBox.childNodes[i].getAttribute("style") + ";border-left: 6px solid red;");
	}
}

function createResultEntityBox(result){
	var resultEntityBox = document.createElement("div");
	resultEntityBox.setAttribute("id", "resultEntityBox");
	resultEntityBox.setAttribute("class", "col-md-3");  
	resultEntityBox.setAttribute("style", "height: 600px");
	
	for(var i = 0; i < result.resultEntityList.length; i ++)
		resultEntityBox.appendChild(createEntityBox(result.resultEntityList[i], 2));
    
	return resultEntityBox;
}