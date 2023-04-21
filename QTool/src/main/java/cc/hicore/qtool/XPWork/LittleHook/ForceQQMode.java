package cc.hicore.qtool.XPWork.LittleHook;

import android.app.AlertDialog;
import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import cc.hicore.HookItemLoader.Annotations.MethodScanner;
import cc.hicore.HookItemLoader.Annotations.UIClick;
import cc.hicore.HookItemLoader.Annotations.UIItem;
import cc.hicore.HookItemLoader.Annotations.VerController;
import cc.hicore.HookItemLoader.Annotations.XPExecutor;
import cc.hicore.HookItemLoader.Annotations.XPItem;
import cc.hicore.HookItemLoader.bridge.BaseXPExecutor;
import cc.hicore.HookItemLoader.bridge.MethodContainer;
import cc.hicore.HookItemLoader.bridge.MethodFinderBuilder;
import cc.hicore.HookItemLoader.bridge.QQVersion;
import cc.hicore.HookItemLoader.bridge.UIInfo;
import cc.hicore.HookItemLoader.core.CoreLoader;
import cc.hicore.ReflectUtils.MClass;
import cc.hicore.ReflectUtils.MField;
import cc.hicore.ReflectUtils.MMethod;
import cc.hicore.qtool.HookEnv;

@XPItem(name = "强制QQ模式", itemType = XPItem.ITEM_Hook, targetVer = QQVersion.QQ_8_9_15, proc = XPItem.PROC_ALL)
public class ForceQQMode {
    CoreLoader.XPItemInfo info;
    String Devices = HookEnv.Config.getString("Set", "QQ_MODE", "手机");
    LinkedHashMap<String, String> DevicesMap = new LinkedHashMap<>();

    {
        DevicesMap.put("手机", "PHONE");
        DevicesMap.put("平板", "TABLET");
        DevicesMap.put("折叠屏(没啥用)", "FOLD");
    }
    @VerController
    @UIItem
    public UIInfo getUI() {
        UIInfo ui = new UIInfo();
        ui.name = "强制QQ模式";
        ui.desc = "点击选择;重启QQ生效,不保证一定有用";
        ui.groupName = "功能辅助";
        ui.targetID = 4;
        ui.type = 1;
        return ui;
    }

    @VerController
    @MethodScanner
    public void FindMethod(MethodContainer container) {
        container.addMethod(MethodFinderBuilder.newFinderByString("hook", "initDeviceType type = ", m -> true));
        //container.addMethod("hook1", MMethod.FindMethod(MClass.loadClass("com.tencent.hippy.qq.utils.HippyUtils"), "initDeviceType", void.class, new Class[]{Context.class}));
    }

    @VerController
    @XPExecutor(methodID = "hook", period = XPExecutor.After)
    public BaseXPExecutor XPWork() {
        return param -> {
            Class<?> loadClass = MClass.loadClass("com.tencent.common.config.DeviceType");
            Class<?> DevicesClass;
            if (loadClass == null) {
                DevicesClass = MClass.loadClass("com.tencent.common.config.pad.DeviceType");
                if (DevicesClass == null) {
                    throw new ClassNotFoundException();
                }
            } else {
                DevicesClass = loadClass;
            }
            Enum<?> type =  MMethod.CallStaticMethod(DevicesClass, "valueOf", DevicesClass, DevicesMap.get(Devices));
            MField.SetField(null, info.scanResult.get("hook").getDeclaringClass(), "b", DevicesClass, type);
        };
    }

    @UIClick
    @VerController
    public void uiClick(Context context) {
        ArrayList<String> choices = new ArrayList<>(DevicesMap.keySet());

        int checkStatus = choices.indexOf(Devices);
        String OldDevices = Devices;

        new AlertDialog.Builder(context, 3)
                .setTitle("选择QQ模式")
                .setSingleChoiceItems(choices.toArray(new String[0]), checkStatus, (dialog, which) -> {
                    Devices = choices.get(which);
                }).setNegativeButton("保存", (dialog, which) -> {
                    HookEnv.Config.setString("Set", "QQ_MODE", Devices);
                })
                .setOnCancelListener((dialog) -> {
                    Devices = OldDevices;
                })
                .show();
    }

}