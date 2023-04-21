package cc.hicore.qtool.QQGroupHelper.FileHelper;

import android.text.TextUtils;
import android.widget.TextView;

import cc.hicore.HookItemLoader.Annotations.MethodScanner;
import cc.hicore.HookItemLoader.Annotations.UIItem;
import cc.hicore.HookItemLoader.Annotations.VerController;
import cc.hicore.HookItemLoader.Annotations.XPExecutor;
import cc.hicore.HookItemLoader.Annotations.XPItem;
import cc.hicore.HookItemLoader.bridge.BaseXPExecutor;
import cc.hicore.HookItemLoader.bridge.MethodContainer;
import cc.hicore.HookItemLoader.bridge.UIInfo;
import cc.hicore.HookItemLoader.core.CoreLoader;
import cc.hicore.ReflectUtils.MClass;
import cc.hicore.ReflectUtils.MField;
import cc.hicore.ReflectUtils.MMethod;

@XPItem(name = "群文件显示下载次数", itemType = XPItem.ITEM_Hook)
public class ShowFileDownloadTimes {
    CoreLoader.XPItemInfo info;

    @VerController
    @UIItem
    public UIInfo getUI() {
        UIInfo ui = new UIInfo();
        ui.name = "群文件显示下载次数";
        ui.type = 1;
        ui.targetID = 4;
        ui.groupName = "功能辅助";
        return ui;
    }

    @VerController
    @MethodScanner
    public void getHookMethod(MethodContainer container) {
        container.addMethod("hook", MMethod.FindMethodByName(MClass.loadClass("com.tencent.mobileqq.filemanager.api.impl.QFileUtilsImpl"), "setFileDescription"));
    }

    @VerController
    @XPExecutor(methodID = "hook")
    public BaseXPExecutor worker() {
        return param -> {
            String size = (String) param.args[4];
            if (TextUtils.isEmpty(size)) return;

            TextView textView = (TextView) param.args[1];
            Object TroopFileInfo = textView.getTag();

            int DownTimes = MField.GetField(TroopFileInfo, "k");
          //  LogUtils.debug("群文件显示下载次数", Arrays.asList(param.args).toString());
            param.args[4] = DownTimes + "次  " + size;
        };
    }
}