/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.RankingsScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

//Lua dependencies
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.io.IOException;
import java.lang.reflect.Field;


public class WndGame extends Window {

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP		= 2;
	
	private int pos;
	
	public WndGame() {
		
		super();

		//settings
		RedButton curBtn;
		addButton( curBtn = new RedButton( Messages.get(this, "settings") ) {
			@Override
			protected void onClick() {
				hide();
				GameScene.show(new WndSettings());
			}
		});
		curBtn.icon(Icons.get(Icons.PREFS));

		// Challenges window
		if (Dungeon.challenges > 0) {
			addButton( curBtn = new RedButton( Messages.get(this, "challenges") ) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndChallenges( Dungeon.challenges, false ) );
				}
			} );
			curBtn.icon(Icons.get(Icons.CHALLENGE_COLOR));
		}

		// Restart
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) {

			addButton( curBtn = new RedButton( Messages.get(this, "start") ) {
				@Override
				protected void onClick() {
					GamesInProgress.selectedClass = Dungeon.hero.heroClass;
					GamesInProgress.curSlot = GamesInProgress.firstEmpty();
					ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
				}
			} );
			curBtn.icon(Icons.get(Icons.ENTER));
			curBtn.textColor(Window.TITLE_COLOR);
			
			addButton( curBtn = new RedButton( Messages.get(this, "rankings") ) {
				@Override
				protected void onClick() {
					InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
					Game.switchScene( RankingsScene.class );
				}
			} );
			curBtn.icon(Icons.get(Icons.RANKINGS));
		}

		// Main menu
		addButton(curBtn = new RedButton(Messages.get(this, "menu")) {
			@Override
			protected void onClick() {
				try {
					Dungeon.saveAll();
				} catch (IOException e) {
					ShatteredPixelDungeon.reportException(e);
				}
				Game.switchScene(TitleScene.class);
			}
		});
		curBtn.icon(Icons.get(Icons.DISPLAY));
		if (SPDSettings.intro()) curBtn.enable(false);
        
        //Lua
       TwoArgFunction luaStaticGet = new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
				Class clazz = (Class) CoerceLuaToJava.coerce(luaValue,Class.class);
                try {
                    Field field = clazz.getDeclaredField(luaValue1.toString());
					field.setAccessible(true);
					return CoerceJavaToLua.coerce(field.get(null));
                } catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException(e);
                }
			}
		};

		ThreeArgFunction luaStaticSet = new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
				Class clazz = (Class) CoerceLuaToJava.coerce(luaValue,Class.class);
				try {
					Field field = clazz.getDeclaredField(luaValue1.toString());
					field.setAccessible(true);
					field.set(null,CoerceJavaToLua.coerce(luaValue2));
				} catch (NoSuchFieldException | IllegalAccessException e) { throw new RuntimeException(e); }
				return LuaValue.NIL;
			}
		};


		ThreeArgFunction luaSet = new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
				Object obj = CoerceLuaToJava.coerce(luaValue,Object.class);
				try {
					Field field = obj.getClass().getDeclaredField(luaValue1.toString());
					field.setAccessible(true);
					field.set(obj,CoerceJavaToLua.coerce(luaValue2));
				} catch (NoSuchFieldException | IllegalAccessException e) { throw new RuntimeException(e); }
				return LuaValue.NIL;
			}
		};


		TwoArgFunction luaGet = new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
				Object obj = CoerceLuaToJava.coerce(luaValue,Object.class);
				System.out.println(obj);
				System.out.println(luaValue1.toString());
				try {
					Field field = obj.getClass().getDeclaredField(luaValue1.toString());
					field.setAccessible(true);
					return CoerceJavaToLua.coerce(field.get(obj));
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		};

		ThreeArgFunction luaClsGet = new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
                Class clazz = null;
                try {
                    clazz = Class.forName(luaValue1.toString());
                } catch (ClassNotFoundException e) {
                    return LuaValue.NIL;
                }
                Object obj = CoerceLuaToJava.coerce(luaValue,Object.class);
				try {
					Field field = clazz.getDeclaredField(luaValue2.toString());
					field.setAccessible(true);
					return CoerceJavaToLua.coerce(field.get(obj));
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		};

		VarArgFunction luaClsSet = new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs varargs) {
				LuaValue luaObject = varargs.arg(1);
				LuaValue luaClassName = varargs.arg(2);
				LuaValue luaName = varargs.arg(3);
				LuaValue luaSetTo = varargs.arg(4);

				Class clazz = null;

				try {
					clazz = Class.forName(luaClassName.toString());
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}

				Object obj = CoerceLuaToJava.coerce(luaObject,Object.class);
				try {
					Field field = clazz.getDeclaredField(luaName.toString());
					Class type = field.getType();
					field.setAccessible(true);
					Object value;
					if (type.equals(float.class)) {
                        value = luaSetTo.tofloat();
                    } else if (type == int.class) {
						value = luaSetTo.toint();
					} else if (type == boolean.class) {
						value = luaSetTo.toboolean();
					} else if (type == long.class) {
						value = luaSetTo.tolong();
					} else if (type == short.class) {
						value = luaSetTo.toshort();
					} else if (type == String.class) {
						value = luaSetTo.toString();
					} else {
						value = CoerceLuaToJava.coerce(luaSetTo,type);
					}
					field.set(obj,value);
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}

				return LuaValue.NIL;
			}
		};


		//settings
		RedButton commandButton;
		addButton( commandButton = new RedButton("神之力量") {
			@Override
			protected void onClick() {
				hide();

				WndTextInput wndTextInput = new WndTextInput("低语","念出咒语:","",999999,true,"动用","放弃"){
					@Override
					public void onSelect(boolean positive, String text) {
						super.onSelect(positive, text);

						Dungeon.luaGlobal.set("game", CoerceJavaToLua.coerce(Dungeon.class));
						Dungeon.luaGlobal.set("print",new Dungeon.LuaPrint());

						//get function for protected or private attr
						Dungeon.luaGlobal.set("sget",luaStaticGet);
						Dungeon.luaGlobal.set("cget",luaClsGet);
						Dungeon.luaGlobal.set("get",luaGet);

						//set function for protected or private attr
						Dungeon.luaGlobal.set("sset",luaStaticSet);
						Dungeon.luaGlobal.set("cset",luaClsSet);
						Dungeon.luaGlobal.set("set",luaSet);

						Dungeon.luaGlobal.set("cls",Dungeon.luaGlobal.load("return function(c) return \"com.shatteredpixel.shatteredpixeldungeonlua.\"..c end").call());

						Dungeon.luaLogBuilder = new StringBuilder();
						//Dungeon.luaGlobal.set("game", CoerceJavaToLua.coerce(Dungeon.class));
						if(positive){
							try {
								LuaValue value = Dungeon.luaGlobal.load(text).call();
								if(! value.isnil()){
									Dungeon.luaLogBuilder.append("\n#return:");
									Dungeon.luaLogBuilder.append(value.toString());
								}
								GameScene.show(new WndStory(Icons.get(Icons.CONTROLLER),"Result",Dungeon.luaLogBuilder.toString()));
							} catch (LuaError e) {
								GameScene.show(new WndError(e.toString()));
							}
						}

					}
				};
				GameScene.show(wndTextInput);
			}
		});
		commandButton.icon(Icons.get(Icons.CONTROLLER));


		resize( WIDTH, pos );
	}
	
	private void addButton( RedButton btn ) {
		add( btn );
		btn.setRect( 0, pos > 0 ? pos += GAP : 0, WIDTH, BTN_HEIGHT );
		pos += BTN_HEIGHT;
	}

	private void addButtons( RedButton btn1, RedButton btn2 ) {
		add( btn1 );
		btn1.setRect( 0, pos > 0 ? pos += GAP : 0, (WIDTH - GAP) / 2, BTN_HEIGHT );
		add( btn2 );
		btn2.setRect( btn1.right() + GAP, btn1.top(), WIDTH - btn1.right() - GAP, BTN_HEIGHT );
		pos += BTN_HEIGHT;
	}
}
