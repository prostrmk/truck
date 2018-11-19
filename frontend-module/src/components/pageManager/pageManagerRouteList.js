﻿import React, {Component} from "react";
import {GoogleApiWrapper, InfoWindow, Map, Marker } from 'google-maps-react';

class ManagerRouteList extends Component {

    constructor(props) {
        super(props);
        this.getRouteList = this.getRouteList.bind(this);
        this.renderMarkers = this.renderMarkers.bind(this);
        this.forceUpdateHandler = this.forceUpdateHandler.bind(this);
        this.onMarkerClick = this.onMarkerClick.bind(this);
        this.onInfoWindowClose = this.onInfoWindowClose.bind(this);
        this.onMapClick = this.onMapClick.bind(this);
        this.addPoint = this.addPoint.bind(this);
        this.deletePoint = this.deletePoint.bind(this);
        this.state = {
            routePoints: [],
            orderId: "",
            point:"",
            pointLevel:0,
            showingInfoWindow: false,
            activeMarker: {},
            selectedPlace: {}
        }

        document.title = "Путевой лист";
    }

    componentDidMount() {
        this.getRouteList().then(data => {
            let level = 0;
            data.forEach(point => {
                if(level < point.pointLevel) {
                    level = point.pointLevel;
                }
            });
            this.setState({routePoints:data, pointLevel:level+1});
        });
    }

    forceUpdateHandler() {
        this.getRouteList().then(data => {
            let level = 0;
            data.forEach(point => {
                if(level < point.pointLevel) {
                    level = point.pointLevel;
                }
            });
            this.setState({routePoints:data, point:"", pointLevel:level+1});
        });
    }
    getRouteList() {
        let split = document.location.href.split('/');
        let id = split[split.length - 1];
        console.log(id);
        return fetch(`http://localhost:8080/api/manager/routeList/${id}`, {
            method: "get",
            headers: {'Auth-token': localStorage.getItem("Auth-token")}
        }).then(function (response) {
            return response.json();
        }).then(function (result) {
            console.log(result);
            return result;
        });
    }

    renderMarkers(routePoint) {
        if (!routePoint) return;
        return <Marker onClick={this.onMarkerClick}
                       name={routePoint.point} position={{lat: routePoint.lat, lng: routePoint.lng}} id={routePoint.id}/>

    }

    deletePoint(props) {
        console.log("delete");
        console.log(props.pointId);
        const ref = this;
        fetch(`http://localhost:8080/api/manager/deletePoint/${props.pointId}`, {method: "DELETE", headers: {'Auth-token': localStorage.getItem("Auth-token")}})
            .then(function(response) {
                return response.json();
            }).then(function(result) {
                if(result === true) {
                    ref.forceUpdateHandler();
                }
            })
            .catch((err) => {
                console.log(err);
            })
    }

    addPoint(lat, lng) {
        let split = document.location.href.split('/');
        let id = split[split.length - 1];
        let ref = this;
       let routePoint = {};
       routePoint.id = null;
       routePoint.point = this.state.point;
       routePoint.pointLevel = this.state.pointLevel;
       routePoint.waybill = null;
       routePoint.lat = lat;
       routePoint.lng = lng;
       console.log(routePoint);

       fetch(`http://localhost:8080/api/manager/${id}/createPoint`, {method:"POST", headers: {'Content-Type':'application/json', 'Auth-token': localStorage.getItem("Auth-token")},
           body: JSON.stringify(routePoint)})
           .then(function(response) {
               return response.json();
           }).then(function(result) {
               if(result === true) {
                   console.log(result);
                   ref.forceUpdateHandler();
               }
           });
    }

    onMarkerClick = (props, marker, event) => {
        console.log(marker);
        this.setState({
            selectedPlace: props,
            activeMarker: marker,
            showingInfoWindow: true
        });
    }
    onInfoWindowClose() {
        this.setState({
            showingInfoWindow: false
        });
    }
    onMapClick = (mapProps, clickEvent, event) => {
        let position = event.latLng;
        this.addPoint(position.lat(), position.lng());
    }
    render() {
        const style = {
            width: '50vw',
            height: '75vh',
            'marginLeft': 'auto',
            'marginRight': 'auto'
        }
        return (
            <Map google={this.props.google}
                 center={{
                     lat: 53.7169,
                     lng: 27.9776
                 }}
                 zoom={14} onClick={this.onMapClick} id="googleMap">
                {
                    this.state.routePoints.map((element) => {
                        return this.renderMarkers(element);
                    })
                }
                <InfoWindow onClose={this.onInfoWindowClose} marker = {this.state.activeMarker } visible = {this.state.showingInfoWindow }>
                    <div>
                        <h3>{this.state.activeMarker.name}</h3>
                        <div className="table_button bg-secondary text-white" onClick={this.deletePoint}>Удалить</div>
                    </div>
                </InfoWindow>
            </Map>
        );

    }
}

export default GoogleApiWrapper({
    api: (process.env.AIzaSyC8b04jlgefJ27fjvs4axnTGGKvYtFemWI)
})(ManagerRouteList)