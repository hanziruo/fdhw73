/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function() {
    'use strict';
    angular
        .module('app')
        .controller('alltaxiController', alltaxiController);

    alltaxiController.$inject = ['$scope', '$filter', 'Taxi', 'messageBag'];

    function alltaxiController($scope, $filter, Taxi, messageBag) {
        //Assign Taxi service to $scope variable
        $scope.taxis = Taxi;
        //Assign Messages service to $scope variable
        $scope.messages = messageBag;

        //Divide taxi list into several sub lists according to the first character of their registration property
        var getHeadings = function(taxis) {
            var headings = {};
            for(var i = 0; i<taxis.length; i++) {
                //Get the first letter of a taxi's registration
                var startsWithLetter = taxis[i].registration.charAt(0).toUpperCase();
                //If we have encountered that first letter before then add the taxi to that list, else create it
                if(headings.hasOwnProperty(startsWithLetter)) {
                    headings[startsWithLetter].push(taxis[i]);
                } else {
                    headings[startsWithLetter] = [taxis[i]];
                }
            }
            return headings;
        };

        //Upon initial loading of the controller, populate a list of Taxis and their letter headings
        $scope.taxis.data = $scope.taxis.query(
            //Successful query
            function(data) {
                $scope.taxis.data = data;
                $scope.taxisList = getHeadings($scope.taxis.data);
                //Keep the taxis list headings in sync with the underlying taxis
                $scope.$watchCollection('taxis.data', function(newTaxis, oldTaxis) {
                    $scope.taxisList = getHeadings(newTaxis);
                });
            },
            //Error
            function(result) {
                for(var error in result.data){
                    $scope.messages.push('danger', result.data[error]);
                }
            }
        );

        //Boolean flag representing whether the details of the taxis are expanded inline
        $scope.details = false;

        //Default search string
        $scope.search = "";

        //Continuously filter the content of the taxis list according to the contents of $scope.search
        $scope.$watch('search', function(newValue, oldValue) {
            $scope.taxisList = getHeadings($filter('filter')($scope.taxis.data, $scope.search));
        });
    }
})();