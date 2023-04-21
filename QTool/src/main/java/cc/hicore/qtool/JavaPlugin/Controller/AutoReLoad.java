package cc.hicore.qtool.JavaPlugin.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cc.hicore.HookItemLoader.Annotations.MethodScanner;
import cc.hicore.HookItemLoader.Annotations.UIItem;
import cc.hicore.HookItemLoader.Annotations.VerController;
import cc.hicore.HookItemLoader.Annotations.XPExecutor;
import cc.hicore.HookItemLoader.Annotations.XPItem;
import cc.hicore.HookItemLoader.bridge.BaseXPExecutor;
import cc.hicore.HookItemLoader.bridge.MethodContainer;
import cc.hicore.HookItemLoader.bridge.MethodFinderBuilder;
import cc.hicore.HookItemLoader.bridge.UIInfo;
import cc.hicore.HookItemLoader.core.CoreLoader;
import cc.hicore.Utils.Utils;

@XPItem(name = "换号重新加载脚本", itemType = XPItem.ITEM_Hook)
public class AutoReLoad {

    CoreLoader.XPItemInfo info;
    ArrayList<PluginInfo> pluginInfoList = new ArrayList<>();
    boolean Switch = false;

    @VerController
    @UIItem
    public UIInfo getUI() {
        UIInfo ui = new UIInfo();
        ui.name = "换号重新加载脚本";
        ui.type = 1;
        ui.targetID = 4;
        ui.groupName = "功能辅助";
        return ui;
    }

    @MethodScanner
    @VerController
    public void FindMethod(MethodContainer container) {
        container.addMethod(MethodFinderBuilder.newFinderByString("LoginOut", "QQAppInterface onDestroy removeNotification", m -> true));
        container.addMethod(MethodFinderBuilder.newFinderByString("Login", "QQRt_entityManagerFactory_create", m -> true));
    }


    @VerController
    @XPExecutor(methodID = "LoginOut")
    public BaseXPExecutor workerBefore() {
        return param -> {
            HashMap<String, PluginInfo> runningInfo = PluginController.runningInfo;
            pluginInfoList.clear();
            for (String PluginVerifyID : runningInfo.keySet()) {
                PluginInfo pluginInfo = runningInfo.get(PluginVerifyID);
                if (pluginInfo != null) {
                    pluginInfoList.add(pluginInfo);
                    new Thread(() -> {
                        PluginController.endPlugin(pluginInfo.PluginID);
                    }
                    ).start();
                }
            }
            this.Switch = true;
        };
    }


    @VerController
    @XPExecutor(methodID = "Login")
    public BaseXPExecutor worker() {
        return param -> {
            if (Switch) {
                Utils.ShowToast("重新登录中,5s后重新加载脚本,数量:" + pluginInfoList.size());
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        Iterator<PluginInfo> it = pluginInfoList.iterator();
                        while (it.hasNext()) {
                            PluginInfo pluginInfo = it.next();
                            while (pluginInfo.IsRunning) {
                                Thread.sleep(1000);
                            }
                            if (PluginController.LoadOnce(pluginInfo)) {
                                it.remove();
                            }
                        }

                        Utils.ShowToast("重新加载脚本完成，失败数量:" + pluginInfoList.size());
                        Switch = false;

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        };
    }

}