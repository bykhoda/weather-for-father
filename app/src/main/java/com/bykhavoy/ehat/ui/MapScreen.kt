package com.bykhavoy.ehat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.ui.components.BackButton
import com.bykhavoy.ehat.ui.components.Chip
import com.bykhavoy.ehat.ui.components.WebPaneHtml
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.Stroke

private data class Overlay(val label: String, val id: String)

private val OVERLAYS = listOf(
    Overlay("Ветер", "wind"),            // leaflet-velocity + Open-Meteo — потоки, без ключа
    Overlay("Температура", "temp_new"),  // OWM — нужен ключ
    Overlay("Осадки", "radar"),          // RainViewer radar — анимированный, без ключа
)

/** Легенда-шкала под активным слоем — чтобы было понятно, что означают цвета. */
private fun legendHtml(sel: String): String {
    fun bar(title: String, grad: String, lo: String, hi: String) =
        "<div id=\"legend\"><div class=\"lt\">$title</div>" +
            "<div class=\"lb\" style=\"background:linear-gradient(90deg,$grad)\"></div>" +
            "<div class=\"ls\"><span>$lo</span><span>$hi</span></div></div>"
    return when (sel) {
        "radar" -> bar("Осадки · радар", "#c9e8ff,#5aa9ff,#3ec46d,#f5e13a,#f0641e,#b71c1c", "слабо", "ливень")
        "satellite" -> bar("Облака · спутник", "#0a1020,#3a4a66,#8a9ab0,#dfe6ef,#ffffff", "ясно", "плотно")
        "wind" -> bar("Ветер, м/с", "#8fd3ff,#59a5f5,#3a6fe0,#7a3ff0,#c0392b", "тихо", "шторм")
        "temp_new" -> bar("Температура, °C", "#3b4cc0,#4a90ff,#3ec46d,#f5e13a,#f0641e,#b71c1c", "холодно", "жарко")
        else -> ""
    }
}

/** Растровая карта (тайлы-картинки) — рисуется на любом железе, включая Denza (без тяжёлого WebGL). */
private fun mapHtml(lat: Double, lon: Double, sel: String, key: String): String = """
<!DOCTYPE html><html><head>
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<style>html,body{margin:0;padding:0;height:100%;width:100%;background:#eef1f4}
#map{position:absolute;top:0;bottom:0;left:0;right:0}
.leaflet-pane,.leaflet-tile,.leaflet-marker-icon,.leaflet-marker-shadow,.leaflet-tile-container,.leaflet-pane>svg,.leaflet-pane>canvas,.leaflet-zoom-box,.leaflet-image-layer,.leaflet-layer{position:absolute;left:0;top:0}
.leaflet-container{overflow:hidden}
.leaflet-tile{visibility:hidden}
.leaflet-tile-loaded{visibility:inherit}
.leaflet-zoom-animated{transform-origin:0 0}
img.leaflet-tile{max-width:none!important;width:256px;height:256px}
.leaflet-pane{z-index:400}.leaflet-tile-pane{z-index:200}.leaflet-overlay-pane{z-index:400}.leaflet-marker-pane{z-index:600}
.leaflet-top,.leaflet-bottom{position:absolute;z-index:1000;pointer-events:none}
.leaflet-top{top:0}.leaflet-bottom{bottom:0}.leaflet-left{left:0}.leaflet-right{right:0}
.leaflet-control{position:relative;z-index:800;pointer-events:auto}
.leaflet-bar a{background:#fff;width:30px;height:30px;line-height:30px;display:block;text-align:center;color:#333;text-decoration:none;font:bold 18px sans-serif;border-bottom:1px solid #ccc}
.leaflet-control-zoom{margin:12px;box-shadow:0 1px 4px rgba(0,0,0,.3);border-radius:6px;overflow:hidden}
.leaflet-control-attribution{background:rgba(255,255,255,.75);padding:0 5px;font-size:9px}
#msg{position:absolute;top:12px;left:50%;transform:translateX(-50%);z-index:1000;background:#fff;border-radius:8px;
padding:8px 12px;font-family:sans-serif;font-size:13px;color:#c0392b;box-shadow:0 1px 4px rgba(0,0,0,.25);display:none;max-width:80%;text-align:center}
#legend{position:absolute;left:12px;bottom:26px;z-index:1000;background:rgba(255,255,255,.94);border-radius:10px;padding:8px 10px;font-family:sans-serif;box-shadow:0 1px 4px rgba(0,0,0,.25);min-width:160px}
#legend .lt{font-size:12px;font-weight:600;color:#333;margin-bottom:6px}
#legend .lb{height:10px;border-radius:5px}
#legend .ls{display:flex;justify-content:space-between;font-size:10px;color:#666;margin-top:3px}</style>
</head><body>
<div id="map"></div>
<div id="msg"></div>
${legendHtml(sel)}
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script src="https://unpkg.com/leaflet-velocity/dist/leaflet-velocity.min.js"></script>
<script>
var lat=$lat, lon=$lon, sel='$sel', key='$key';
function showMsg(t){var m=document.getElementById('msg');m.textContent=t;m.style.display='block';}
function sizeMap(){var e=document.getElementById('map');e.style.width=window.innerWidth+'px';e.style.height=window.innerHeight+'px';}
sizeMap();
try{
  if(typeof L==='undefined'){showMsg('Не загрузилась карта (Leaflet)');}
  var map=L.map('map',{zoomControl:true,attributionControl:true}).setView([lat,lon],7);
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',
    {maxZoom:18,attribution:'© OpenStreetMap'}).addTo(map);
  L.circleMarker([lat,lon],{radius:6,color:'#e0553c',fillColor:'#e0553c',fillOpacity:1,weight:2}).addTo(map);
  if(sel==='radar' || sel==='satellite'){
    fetch('https://api.rainviewer.com/public/weather-maps.json').then(function(r){return r.json();}).then(function(d){
      var host=d.host;
      var fr = (sel==='radar') ? (d.radar.past||[]).concat(d.radar.nowcast||[]) : ((d.satellite&&d.satellite.infrared)||[]);
      var suffix = (sel==='radar') ? '/256/{z}/{x}/{y}/2/1_1.png' : '/256/{z}/{x}/{y}/0/0_0.png';
      if(!fr.length){showMsg('Слой сейчас недоступен');return;}
      var layers=fr.map(function(f){return L.tileLayer(host+f.path+suffix,{opacity:0,zIndex:5});});
      layers.forEach(function(l){l.addTo(map);});
      var i=0; function show(k){layers.forEach(function(l,j){l.setOpacity(j===k?0.8:0);});}
      show(0); setInterval(function(){i=(i+1)%layers.length; show(i);},600);
    }).catch(function(){showMsg('Нет связи с сервисом карт');});
  } else if(sel==='wind'){
    if(typeof L.velocityLayer==='undefined'){ showMsg('Ветер: плагин не загрузился'); }
    var la1=lat+5, la2=lat-5, lo1=lon-5, lo2=lon+5, nx=10, ny=10;
    var dx=(lo2-lo1)/(nx-1), dy=(la1-la2)/(ny-1);
    var lats=[], lons=[];
    for(var j=0;j<ny;j++){ for(var q=0;q<nx;q++){ lats.push((la1-j*dy).toFixed(3)); lons.push((lo1+q*dx).toFixed(3)); } }
    var wu='https://api.open-meteo.com/v1/forecast?latitude='+lats.join(',')+'&longitude='+lons.join(',')+'&current=wind_speed_10m,wind_direction_10m&wind_speed_unit=ms';
    fetch(wu).then(function(r){return r.json();}).then(function(arr){
      if(!Array.isArray(arr)){ arr=[arr]; }
      var uu=[], vv=[];
      for(var k=0;k<arr.length;k++){ var c=arr[k].current||{}; var sp=c.wind_speed_10m||0; var dr=(c.wind_direction_10m||0)*Math.PI/180; uu.push(-sp*Math.sin(dr)); vv.push(-sp*Math.cos(dr)); }
      function comp(num,data){ return {header:{parameterUnit:'m.s-1',parameterCategory:2,parameterNumber:num,dx:dx,dy:dy,la1:la1,lo1:lo1,la2:la2,lo2:lo2,nx:nx,ny:ny,refTime:new Date().toISOString()},data:data}; }
      L.velocityLayer({displayValues:false,data:[comp(2,uu),comp(3,vv)],maxVelocity:18,velocityScale:0.012,
        colorScale:['#8fd3ff','#59a5f5','#3a6fe0','#7a3ff0','#c0392b']}).addTo(map);
    }).catch(function(){ showMsg('Ветер: нет связи с Open-Meteo'); });
  } else {
    if(!key){ showMsg('Для слоя «Температура» нужен ключ OpenWeatherMap (Фильтры → Ключ карты)'); }
    else {
      var owm=L.tileLayer('https://tile.openweathermap.org/map/'+sel+'/{z}/{x}/{y}.png?appid='+key,{opacity:0.9,zIndex:5});
      var errShown=false;
      owm.on('tileerror',function(){ if(!errShown){errShown=true; showMsg('Слой не загрузился — проверь ключ OWM (новый активируется до ~2 часов)');} });
      owm.addTo(map);
    }
  }
  setTimeout(function(){sizeMap();map.invalidateSize();},200);
  setTimeout(function(){sizeMap();map.invalidateSize();},800);
  window.addEventListener('resize',function(){sizeMap();map.invalidateSize();});
}catch(e){ showMsg('Ошибка карты: '+e); }
</script>
</body></html>
""".trimIndent()

@Composable
fun MapScreen(lat: Double, lon: Double, locationName: String, owmKey: String, onBack: () -> Unit) {
    var overlay by remember { mutableStateOf("wind") }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Карта · $locationName", color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OVERLAYS.forEach { o -> Chip(o.label, active = o.id == overlay) { overlay = o.id } }
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Stroke))

        WebPaneHtml(mapHtml(lat, lon, overlay, owmKey), Modifier.fillMaxSize())
    }
}
