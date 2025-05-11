[![Typing SVG](https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&pause=1000&color=F70E34&width=435&lines=%E5%A6%82%E6%9E%9C%E8%AF%A5%E9%A1%B9%E7%9B%AE%E5%AF%B9%E4%BD%A0%E6%9C%89%E5%B8%AE%E5%8A%A9%E7%9A%84%E8%AF%9D%EF%BC%8C%E6%AC%A2%E8%BF%8E%E7%82%B9%E4%B8%AA+star+%EF%BC%81)](https://git.io/typing-svg)

## 实现效果

### KMZ 文件结构

![kmz-01.gif](https://s2.loli.net/2024/11/19/kznUqoeSa7CbBAX.gif)

### template.kml 文件内容

![kmz-02.png](https://s2.loli.net/2024/11/19/tDBySCuWoM3mUpf.png)

## 特点

### 1、航线 KMZ 文件生成

基于大疆最新航线文件格式标准 v1.11.3 版本开发，生成符合大疆标准的 KMZ 航线文件，可直接导入到 DJI Pilot 2 或机场等地面站软件中使用。
支持灵活配置各种航线参数，包括：

* 航线结束动作： 自定义航线飞行结束后无人机的行为（例如：悬停、返航、降落）。
* 失控动作： 设置无人机在失去遥控信号时的应对措施（例如：自动返航、悬停）。
* 飞行高度和速度： 精确控制航线飞行的高度和速度，确保飞行安全和效率。
* 航点转弯模式： 选择合适的转弯方式（例如：直线飞行、曲线飞行），优化飞行轨迹。
* 偏航角模式： 控制无人机在航点之间的偏航角度，满足不同的拍摄需求。
* 航点动作： 为每个航点单独设置动作，例如拍照、录像、悬停等，实现复杂的飞行任务。

### 2、航线 KMZ 文件解析

支持解析已有的 KMZ 航线文件，方便用户导入、查看和编辑航线数据，提高航线管理的效率和灵活性。

### 3、易于使用和扩展

提供结构清晰、注释完善完整的 Demo 代码，帮助用户快速上手，进行二次开发。

## 代码分支

* waypoint：航点飞行是目前使用最多的场景，因此单独维护该分支，仅支持生成和解析航点飞行模板功能。
* main：主分支，在 waypoint 分支的基础上，实现更多功能，目前开发建图航拍等模板的生成和解析功能。

## 目录结构

* file/kmz：存放生成的 KMZ 文件
* domain/kml：XStream 注解的 Java Bean
* domain/*.Req：前端请求参数对象
* enums：航线文件元素标签取值枚举
* util/RouteFileUtils：生成和解析 KMZ 航线文件的代码都在这个工具类里

## 接口参数示例

### 生成航线 KMZ 文件

```json
{
  "templateType": "waypoint",
  "takeOffRefPoint": "39.02706649854977,115.95067260218747,0",
  "droneType": 91,
  "subDroneType": 0,
  "payloadType": 81,
  "payloadPosition": 0,
  "imageFormat": "wide",
  "finishAction": "autoLand",
  "exitOnRcLostAction": "goBack",
  "globalHeight": 100,
  "autoFlightSpeed": 10,
  "waypointHeadingReq": {
    "waypointHeadingMode": "followWayline"
  },
  "waypointTurnReq": {
    "waypointTurnMode": "toPointAndStopWithDiscontinuityCurvature"
  },
  "gimbalPitchMode": "usePointSetting",
  "routePointList": [
    {
      "routePointIndex": 0,
      "longitude": 115.94831278397191,
      "latitude": 39.02661859808053,
      "isStartAndEndPoint": true,
      "timeInterval": 5,
      "endIntervalRouteIndex": 1,
      "actions": [
        {
          "actionIndex": 0,
          "takePhotoType": 0,
          "useGlobalImageFormat": 1
        }
      ]
    },
    {
      "routePointIndex": 1,
      "longitude": 115.94831278397191,
      "latitude": 39.02661859808053,
      "isStartAndEndPoint": false
    },
    {
      "routePointIndex": 2,
      "longitude": 115.94983615244982,
      "latitude": 39.025246778993534,
      "isStartAndEndPoint": false,
      "actions": [
        {
          "actionIndex": 0,
          "hoverTime": 5
        },
        {
          "actionIndex": 1,
          "useGlobalImageFormat": 1,
          "takePhotoType": 1
        }
      ]
    },
    {
      "routePointIndex": 3,
      "longitude": 115.9471013661498,
      "latitude": 39.0252061309485,
      "isStartAndEndPoint": true,
      "actions": [
        {
          "actionIndex": 0,
          "startRecord": true,
          "useGlobalImageFormat": 1
        },
        {
          "actionIndex": 0,
          "stopRecord": true
        }
      ]
    }
  ]
}


```

### 编辑航线 KMZ 文件

```json
{
  "templateType": "waypoint",
  "takeOffRefPoint": "39.02706649854977,115.95067260218747,0",
  "droneType": 91,
  "subDroneType": 0,
  "payloadType": 81,
  "payloadPosition": 0,
  "imageFormat": "wide",
  "finishAction": "autoLand",
  "exitOnRcLostAction": "goBack",
  "globalHeight": 100,
  "autoFlightSpeed": 10,
  "waypointHeadingReq": {
    "waypointHeadingMode": "followWayline"
  },
  "waypointTurnReq": {
    "waypointTurnMode": "toPointAndStopWithDiscontinuityCurvature"
  },
  "gimbalPitchMode": "usePointSetting",
  "routePointList": [
    {
      "routePointIndex": 0,
      "longitude": 115.94831278397191,
      "latitude": 39.02661859808053,
      "isStartAndEndPoint": true,
      "timeInterval": 5,
      "endIntervalRouteIndex": 1,
      "actions": [
        {
          "actionIndex": 0,
          "takePhotoType": 0,
          "useGlobalImageFormat": 1
        }
      ]
    },
    {
      "routePointIndex": 1,
      "longitude": 115.94831278397191,
      "latitude": 39.02661859808053,
      "isStartAndEndPoint": false
    },
    {
      "routePointIndex": 2,
      "longitude": 115.94983615244982,
      "latitude": 39.025246778993534,
      "isStartAndEndPoint": false,
      "actions": [
        {
          "actionIndex": 0,
          "hoverTime": 5
        },
        {
          "actionIndex": 1,
          "useGlobalImageFormat": 1,
          "takePhotoType": 1
        }
      ]
    },
    {
      "routePointIndex": 3,
      "longitude": 115.9471013661498,
      "latitude": 39.0252061309485,
      "isStartAndEndPoint": true,
      "actions": [
        {
          "actionIndex": 0,
          "startRecord": true,
          "useGlobalImageFormat": 1
        },
        {
          "actionIndex": 0,
          "stopRecord": true
        }
      ]
    }
  ]
}
```

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=SongJian-99/dj-uav&type=Date)](https://star-history.com/#SongJian-99/dj-uav&Date)
