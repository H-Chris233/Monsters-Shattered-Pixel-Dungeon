//Command
package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.TextInput;
import com.watabou.noosa.ui.Component;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class CommandScene extends PixelScene{

    Globals globals = JsePlatform.standardGlobals();
    @Override
    public void create() {
        super.create();

        final float colWidth = 120;
        final float fullWidth = colWidth * (landscape() ? 2 : 1);

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);

        //darkens the arches
        add(new ColorBlock(w, h, 0x88000000));

        ScrollPane list = new ScrollPane(new Component());
        add(list);
        list.setRect(0,0,w,h);

        Component content = list.content();
        content.clear();

        TextInput input = new TextInput(Chrome.get(Chrome.Type.TOAST_WHITE), true, 10);
        input.setRect(5,h/16,w - 10,  h /4 - 10);
        list.content().add(input);

        RedButton runBtn = new RedButton("Run"){
            @Override
            protected void onClick() {
                super.onClick();

                LuaValue chunk = globals.load(input.getText());
                LuaValue value = chunk.call();


            }
        };
        runBtn.setRect(input.left(),input.bottom(),input.width(),h/16);
        list.content().add(runBtn);


        ExitButton btnExit = new ExitButton();
        btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
        add( btnExit );

    }


    @Override
    protected void onBackPressed() {
        ShatteredPixelDungeon.switchScene(TitleScene.class);
    }
}
