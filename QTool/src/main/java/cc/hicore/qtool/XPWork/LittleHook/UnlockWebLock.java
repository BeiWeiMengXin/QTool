package cc.hicore.qtool.XPWork.LittleHook;

import android.os.Bundle;

import java.lang.reflect.Method;
import java.net.URLDecoder;

import cc.hicore.HookItemLoader.Annotations.MethodScanner;
import cc.hicore.HookItemLoader.Annotations.UIItem;
import cc.hicore.HookItemLoader.Annotations.VerController;
import cc.hicore.HookItemLoader.Annotations.XPExecutor;
import cc.hicore.HookItemLoader.Annotations.XPItem;
import cc.hicore.HookItemLoader.bridge.BaseXPExecutor;
import cc.hicore.HookItemLoader.bridge.MethodContainer;
import cc.hicore.HookItemLoader.bridge.MethodFinderBuilder;
import cc.hicore.HookItemLoader.bridge.UIInfo;
import cc.hicore.Utils.Utils;

@XPItem(name = "解除风险网址拦截", itemType = XPItem.ITEM_Hook, proc = XPItem.PROC_ALL)
public class UnlockWebLock {
    String last;
    private static String GetStringMiddle(String str, String before, String after) {
        int index1 = str.indexOf(before);
        if (index1 == -1) return null;
        int index2 = str.indexOf(after, index1 + before.length());
        if (index2 == -1) return null;
        return str.substring(index1 + before.length(), index2);
    }

    @VerController
    @UIItem
    public UIInfo getUI() {
        UIInfo ui = new UIInfo();
        ui.name = "解除风险网址拦截";
        ui.groupName = "功能辅助";
        ui.targetID = 1;
        ui.type = 1;
        return ui;
    }

    @VerController
    @MethodScanner
    public void getHookMethod(MethodContainer container) {
        container.addMethod(MethodFinderBuilder.newFinderByString("hook", "com.tencent.biz.troop.TroopMemberApiService", m -> ((Method) m).getReturnType().equals(void.class) && ((Method) m).getParameterCount() == 2 && ((Method) m).getParameterTypes()[1].equals(Bundle.class)));
    }

    @VerController
    @XPExecutor(methodID = "hook")
    public BaseXPExecutor worker() {
        return param -> {
            int id = ((Integer) param.args[0]);
            if (id != 121) return;

            Bundle bundle = (Bundle) param.args[1];
            if (bundle == null) return;

            int jumpResult = bundle.getInt("jumpResult", 0);
            if (jumpResult == 0) return;

            String jumpUrl = bundle.getString("jumpUrl", "");
            String RedirectUrl = URLDecoder.decode(GetStringMiddle(jumpUrl, "url=", "&"), "UTF-8");
            if (RedirectUrl == null) return;

            if (!RedirectUrl.equals(last)) {
                last = RedirectUrl;
                Utils.ShowToast("已解除拦截");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        last = null;
                    } catch (InterruptedException ignored) {
                    }
                });
            }
            bundle.putInt("jumpResult", 0);
            bundle.putString("jumpUrl", "");
        };
    }
}
