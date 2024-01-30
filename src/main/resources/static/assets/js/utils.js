var drawTable = function (data, target) {

    target.clear();
    switch (data.kind) {
        case "StorageClassList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var parameters = '';
                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }else{
                    labels += '-';
                }

                if(item.parameters != undefined){
                    var keys = Object.keys(item.parameters);
                    $.each(keys, function (idx, itm) {
                        parameters += itm + ':' + item.parameters[itm];
                        parameters += idx != keys.length - 1 ? '\n' : '';
                    });
                }else{
                    parameters += '-'
                }


                target.row
                    .add([
                        item.metadata.name,
                        labels,
                        item.provisioner,
                        parameters,
                        item.metadata.creationTimestamp
                    ])
                    .draw();

            });
            break;
        case "CronJobList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var images = '';
                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }else{
                    labels = '-';
                }

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.spec.schedule,
                        item.spec.suspend,
                        0,
                        item.status.lastScheduleTime,
                        item.metadata.creationTimestamp
                    ])
                    .draw();

            });
            break;
        case "DaemonSetList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var images = '';
                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                $.each(item.spec.template.spec.containers, function (idx, itm) {
                    images += itm.image;
                    images += idx != item.spec.template.spec.containers.length - 1 ? '\n' : '';
                });

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.status.numberReady +'/'+ item.status.desiredNumberScheduled,
                        item.metadata.creationTimestamp,
                        images
                    ])
                    .draw();

            });
            break;
        case "DeploymentList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var images = '';
                if (item.metadata.labels != undefined) {
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                $.each(item.spec.template.spec.containers, function (idx, itm) {
                    images += itm.image;
                    images += idx != item.spec.template.spec.containers.length - 1 ? '\n' : '';
                });

                console.log(index + ':' + item);
                // 创建删除按钮的HTML代码
                var deleteButton = '';
                deleteButton += 'a';

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.status.readyReplicas + '/' + item.status.replicas,
                        item.metadata.creationTimestamp,
                        images,

                    ])
                    .draw();
            });
            break;
        case "JobList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var images = '';
                var status = '';
                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                $.each(item.spec.template.spec.containers, function (idx, itm) {
                    images += itm.image;
                    images += idx != item.spec.template.spec.containers.length - 1 ? '\n' : '';
                });

                $.each(item.status.conditions,function (idx, itm) {
                   status += itm.type + ':' + itm.status;
                    status += idx != item.status.conditions.length - 1 ? '\n' : '';
                });

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        status,
                        item.metadata.creationTimestamp,
                        images
                    ])
                    .draw();

            });
            break;
        case "PodList" :
            $.each(data.items, function (index, item) {
                var resetCount = 0;

                $.each(item.status.containerStatuses, function (idx, itm) {
                   resetCount += itm.restartCount;
                });


                    var image = '';
                    //image=item.status.containerStatuses[0].image;
                    var keys = Object.keys(item.status.containerStatuses);
                        $.each(keys, function (idx, itm) {
                            image += item.status.containerStatuses[itm].image;
                                //'\n' mei you xiao guo? why?
                            //image += idx != keys.length - 1 ? '\n' : '.';
                        });




                target.row
                    .add([
                        item.metadata.name,
                        image,
                        item.metadata.namespace,
                        item.status.hasOwnProperty("hostIP") ? item.status.hostIP : "-",
                        item.status.phase,
                        resetCount,
                        item.metadata.creationTimestamp,
                        'log'
                    ])
                    .draw();


            });
            break;
        case "ReplicaSetList":
            $.each(data.items, function (index, item) {
                var images = '';
                var labels = '';

                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                $.each(item.spec.template.spec.containers, function (idx, itm) {
                    images += itm.image;
                    images += idx != item.spec.template.spec.containers.length - 1 ? '\n' : '';
                });


                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.status.replicas + "/" + item.status.readyReplicas,
                        item.metadata.creationTimestamp,
                        images
                    ])
                    .draw();

            });
            break;
        case "ReplicationControllerList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var images = '';
                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                $.each(item.spec.template.spec.containers, function (idx, itm) {
                    images += itm.image;
                    images += idx != item.spec.template.spec.containers.length - 1 ? '\n' : '';
                });

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.status.readyReplicas +'/'+ item.status.replicas,
                        item.metadata.creationTimestamp,
                        images
                    ])
                    .draw();

            });
            break;
        case "StatefulSetList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var images = '';
                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                $.each(item.spec.template.spec.containers, function (idx, itm) {
                    images += itm.image;
                    images += idx != item.spec.template.spec.containers.length - 1 ? '\n' : '';
                });

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.status.currentReplicas +'/'+ item.status.replicas,
                        item.metadata.creationTimestamp,
                        images
                    ])
                    .draw();

            });
            break;
        case "IngressList":
            $.each(data.items, function (index, item) {

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        '-',
                        item.metadata.creationTimestamp
                    ])
                    .draw();

            });
            break;
        case "ServiceList":
            $.each(data.items, function (index, item) {
                var labels = '';

                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }


                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.spec.clusterIP,
                        '-',
                        '-',
                        item.metadata.creationTimestamp,
                    ])
                    .draw();

            });
            break;
        case "ConfigMapList":
            $.each(data.items, function (index, item) {
                var labels = '';

                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        labels,
                        item.metadata.creationTimestamp,
                    ])
                    .draw();

            });
            break;
        case "PersistentVolumeClaimList":
            $.each(data.items, function (index, item) {
                var labels = '';
                var accessModes = '';

                if(item.metadata.labels != undefined){
                    var keys = Object.keys(item.metadata.labels);
                    $.each(keys, function (idx, itm) {
                        labels += itm + ':' + item.metadata.labels[itm];
                        labels += idx != keys.length - 1 ? '\n' : '';
                    });
                }

                $.each(item.spec.accessModes, function (idx, itm) {
                    accessModes += itm;
                    accessModes += idx != item.spec.accessModes.length - 1 ? '\n' : '';
                });


                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        item.status.phase,
                        item.spec.hasOwnProperty("volumeName") ? item.spec.volumeName : '-',
                        item.status.hasOwnProperty("capacity") ? item.status.capacity.storage : '-',
                        accessModes,
                        item.spec.storageClassName,
                        item.metadata.creationTimestamp
                    ])
                    .draw();

            });
            break;
        case "SecretList":
            $.each(data.items, function (index, item) {
                target.row
                    .add([
                        item.metadata.name,
                        item.metadata.namespace,
                        item.type,
                        item.metadata.creationTimestamp,
                    ])
                    .draw();

            });
            break;
    }


}