var iris = angular.module("irisApp");

iris.constant("labelURL", "/project/{projectID}/image/{imageID}/label/");

/**
 * This service provides common (shared) functionality for the client.
 */
iris.factory("sharedService",function($http, $rootScope, $location, $window, $log, labelURL, cytomineService) {
	
		return {
			// firing an alert which will be handled by the alertCtrl
			addAlert : function(message, alertType){
				$rootScope.$broadcast("addAlert", { msg : message, type : alertType });
			},
			
			/////////////////////////////////
			// JUST FOR DEBUG!!!
			getCurrentUser : function(callbackSuccess, callbackError){
				$http.get("http://beta.cytomine.be/api/user/current.json")
				.success(function(data){
					$log.debug(data);
					if (callbackSuccess){
						callbackSuccess(data);
					}
				})
				.error(function(data, error, header, config){
					$log.error(error)
					if (callbackError){
						callbackError(error);
					}
				});
			},
			/////////////////////////////////
			
			moveToLabelingPage : function(projectID, imageID){
				var url = labelURL.replace("{projectID}", projectID)
				.replace("{imageID}", imageID);

				$location.path(url, false);
				console.log($location.absUrl());
				$window.location.href = $location.absUrl();
				$window.location.reload();
			},
			
			// create the today variable in "long" format
			today : new Date().getTime(),
			
			// string function for start with
			strStartsWith : function(string, prefix) {
				  return string.indexOf(prefix) == 0;
			},
				
			// string function for ends with
			strEndsWith : function(string, suffix) {
				  return string.indexOf(suffix, string.length - suffix.length) != -1;
			},

			// string function testing if string contains a substring
			strContains : function(string, substr) {
				  return string.indexOf(substr) != -1;
			},
		};
		
	});