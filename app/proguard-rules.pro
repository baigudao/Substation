# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in d:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}



    #------------------------------------------------------------------------
    #-------------------------------------------基本不用动区域--------------------------------------------
    #---------------------------------基本指令区----------------------------------
    #打印日志，保留异常，源文件行数信息。
    -printmapping proguardMapping.txt
    #-optimizations !code/simplification/cast,!field/*,!class/merging/*
    -keepattributes *Annotation*,InnerClasses
    -keepattributes Signature
    -keepattributes SourceFile,LineNumberTable
    -renamesourcefileattribute SourceFile
    -keepattributes Exceptions,SourceFile
    #Shrink Options
    #不缩减代码  shrink，测试后发现会将一些无效代码给移除，即没有被显示调用的代码
    #-dontshrink
    -ignorewarnings						# 忽略警告，避免打包时某些警告出现
    -optimizationpasses 7				# 指定代码的压缩级别[0-7]
    -dontusemixedcaseclassnames			# 表示混淆时不使用大小写混合类名。
    -dontskipnonpubliclibraryclasses	# 是否混淆第三方jar 表示不跳过library中的非public的类。
    -dontskipnonpubliclibraryclassmembers
    #-dontpreverify                      # 混淆时是否做预校验 表示不进行预校验。这个预校验是作用在Java平台上的，Android平台上不需要这项功能，去掉之后还可以加快混淆速度。
    -verbose                            # 混淆时是否记录日志 表示打印混淆的详细信息。
    -dontnote
    -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*        # 混淆时所采用的算法
    # 以下两个命令配合让类的路径给删除了
    -allowaccessmodification
    #优化 不优化输入的类文件  表示不进行优化，建议使用此选项，因为根据proguard-android-optimize.txt中的描述，优化可能会造成一些潜在风险，不能保证在所有版本的Dalvik上都正常运行。
    -dontoptimize
    -repackageclasses #把执行后的类重新放在某一个目录下，后跟一个目录名
    #记录生成的日志数据,gradle build时在本项目根目录输出
    #apk 包内所有 class 的内部结构
    -dump class_files.txt
    #未混淆的类和成员
    -printseeds seeds.txt
    #列出从 apk 中删除的代码
    -printusage unused.txt
    #----------------------------------------------------------------------------

    #-------------------------------------------定制化区域----------------------------------------------
    #---------------------------------1.实体类---------------------------------
    #-------------------------------------------------------------------------
    #---------------------------------2.第三方包-------------------------------
    #annotation///50
    -dontwarn android.support.annotation.**
    -keep class android.support.annotation.** {*;}
    #javax.annotation//28 30
    -dontwarn javax.annotation.**
    -keep class javax.annotation.**{*;}
    #support//6 7 42
    -keep class android.support.v7.** {*;}
    -keep interface android.support.v7.** { *; }
    ## 51 52  53 54 56
    -keep class android.support.v4.** {*;}
    -dontwarn android.support.v4.**     #缺省proguard 会检查每一个引用是否正确，但是第三方库里面往往有些不会用到的类，没有正确引用。如果不配置的话，系统就会报错。
    #55
     -keep class android.support.v4.media.** {*;}
     -dontwarn android.support.v4.media.**
    #
#    -dontwarn android.os.**
    #design-23   10
    -dontwarn android.support.design.**
    -keep class android.support.design.** {*;}
    #junit//
#    -dontwarn junit.**
#    -keep class junit.** {*;}
#    -dontwarn org.junit.**
#    -keep class org.junit.** {*;}
    #rx// 46
    -dontwarn rx.android.**
    -keep class rx.android.** {*;}
    #rxbinding 47
    -dontwarn com.jakewharton.rxbinding.**
    -keep class com.jakewharton.rxbinding.** {*;}
    #rxbinding.support 48
    -dontwarn com.jakewharton.rxbinding.support.v4.**
    -keep class com.jakewharton.rxbinding.support.v4.** {*;}
    #rx 49
    -dontwarn rx.**
    -keep class rx.** {*;}
    #retrofit2// 43
    -dontwarn retrofit2.**
    -keep class retrofit2.**{*;}
    #adapter-rxjava//4
    -dontwarn retrofit2.adapter.rxjava**
    -keep class retrofit2.adapter.rxjava.** { *; }
    -keepattributes Signature
    -keepattributes Exceptions
    #javax,inject//29
    -dontwarn javax.inject.**
    -keep class javax.inject.**{*;}
     #-keep class java.io.FileDescriptor.**{*;}
     -keep class android.serialport.api.**{*;}
    #gson//8
    -dontwarn retrofit2.converter.gson.**
    -keep class retrofit2.converter.gson.** {*;}
    #gson-2.7//21
    -dontwarn com.google.gson.**
    -keep class com.google.gson.**{*;}
    #pagerslidingtabstrip//40
    -dontwarn com.astuetz.**
    -keep class com.astuetz.** {*;}
    #familiarrecyclerview//15
    -dontwarn cn.iwgang.familiarrecyclerview.**
    -keep class cn.iwgang.familiarrecyclerview.** {*;}
    #recyclerrefreshlayout//41
    -dontwarn com.dinuscxj.**
    -keep class com.dinuscxj.** {*;}
    #library-1.1.4// 31
    -dontwarn me.zhanghai.android.materialprogressbar.**
    -keep class me.zhanghai.android.materialprogressbar.** {*;}
    #library-2.4.0//32
    -dontwarn com.nineoldandroids.**
    -keep class com.nineoldandroids.**{*;}
    #materialishprogress//34
    -dontwarn com.pnikosis.materialishprogress.**
    -keep class com.pnikosis.materialishprogress.** {*;}
    #multidex//35
    -dontwarn android.support.multidex.**
    -keep class android.support.multidex.**{*;}
    #multidex.instrumentation 36
    -dontwarn android.support.multidex.instrumentation.**
    -keep class android.support.multidex.instrumentation.**{*;}
    -dontwarn com.android.test.runner.**
    -keep class com.android.test.runner.**{*;}
    #rules//44 45
    -dontwarn android.support.test.**
    -keep class android.support.test.**{*;}
    #IndexRecyclerView//26
    -dontwarn com.jiang.android.lib.**
    -keep class com.jiang.android.lib.** {*;}
    #eventBus//13
    -dontwarn de.greenrobot.event.**
    -keep class de.greenrobot.event.** {*;}
    -keepclassmembers class ** {
         public void onEvent*(**);
    }
    #greendao3.2.0,此是针对3.2.0，如果是之前的，可能需要更换下包名//19  20
    -dontwarn org.greenrobot.greendao.**
    -keep class org.greenrobot.greendao.**{*;}
    -keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
    }
    -keep class **$Properties
    #glide//18
    -dontwarn com.bumptech.glide.**
    -keep class com.bumptech.glide.** {*;}
    #javawriter//27
    -dontwarn com.squareup.javawriter.**
    -keep class com.squareup.javawriter.**{*;}
    #
    #floatingsearchview//17
    -dontwarn com.arlib.floatingsearchview.**
    -keep class com.arlib.floatingsearchview.** {*;}
    #floatingactionbutton//16
    -dontwarn com.melnykov.fab.**
    -keep class com.melnykov.fab.** {*;}
    #mq//
    -dontwarn com.rabbitmq.**
    -keep class com.rabbitmq.** {*;}
    #hamcrest//22  23  24
    -dontwarn org.hamcrest.**
    -keep class org.hamcrest.**{*;}
    #material-dialogs//9
    -dontwarn com.afollestad.materialdialogs.**
    -keep class com.afollestad.materialdialogs.** {*;}
    #imagespickers//25
    -dontwarn com.jaiky.imagespickers.**
    -keep class com.jaiky.imagespickers.** {*;}
    #okhttputils//38
    -dontwarn com.zhy.http.okhttp.**
    -keep class com.zhy.http.okhttp.**{*;}
    #okhttp3.logging-interceptor//33
    -dontwarn okhttp3.logging.**
    -keep class okhttp3.logging.** { *;}
    #okhttp3//37
    -dontwarn okhttp3.**
    -keep class okhttp3.**{*;}
    #okio 39
    -dontwarn okio.**
    -keep class okio.**{*;}
    #espressc-core//11 12
    -dontwarn android.support.test.espresso.**
    -keep class android.support.test.espresso.**{*;}
    -dontwarn com.google.**
    -keep class com.google.**{*;}
    -dontwarn dagger.internal.**
    -keep class dagger.internal.**{*;}
    #exposed-instrumentation//14
    -dontwarn android.app.**
    -keep class android.app.**{*;}
    -dontwarn support.test.internal.runner.hidden.**
    -keep class support.test.internal.runner.hidden.**{*;}
    #viewpropertyobjectanimator//58
    -dontwarn com.bartoszlipinski.viewpropertyobjectanimator.**
    -keep class com.bartoszlipinski.viewpropertyobjectanimator.**{*;}

    #Compatibility library######
    -keep public class * extends android.support.v4.app.Fragment
    -keep public class * extends android.app.Fragment
    # Only required if you use AsyncExecutor
    -keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
        <init>(java.lang.Throwable);
    }
    #表示不混淆任何包含native方法的类的类名以及native方法名，这个和我们刚才验证的结果是一致的
    -keepclasseswithmembernames class * {
        native <methods>;
    }
    #drawable//5  57
    -dontwarn android.support.graphics.drawable.**
    -keep class android.support.graphics.drawable.** {*;}

    #兼容5.0.0及以上版本的SDK
    -keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
    }
    # Gson uses generic type information stored in a class file when working with fields. Proguard
    # removes such information by default, so configure it to keep all of it.
    -keepattributes Signature
    # Gson specific classes
    -keep class sun.misc.Unsafe { *; }
   # -keep class com.taisau.facerecoginition2.bean.**{*;}

    #==================protobuf======================
    -dontwarn com.google.**
    -keep class com.google.protobuf.** {*;}
    #xutils3//59
   # -dontwarn android.backport.webp.**
    #-keep class android.backport.webp.**{*;}
    #-dontwarn org.xutils.**
    #-keep class org.xutils.**{*;}
     #model
    -keep interface substation.http.*{*;}
    -keep class substation.http.*{*;}
    -keep public class substation.model.*{*;}
   # -keep interface com.taisau.facerecoginition2..ui.user.login.LoginContract.Model{*;}
    #-keep public class * implements com.taisau.facerecoginition2.mvpFrame.baseMvp.BaseModel
    #-keep interface com.taisau.facerecoginition2.mvpFrame.baseMvp.BaseModel{*;}
    #-keepclassmembers public class com.taisau.facerecoginition2.ui.user.login.LoginModel{
     ##    }
    #---------------------------------4.反射相关的类和方法-----------------------
    #---------------------------------默认保留区---------------------------------
    -keep public class * extends android.app.Activity
    -keep public class * extends android.app.Application
    -keep public class * extends android.app.Service
    -keep public class * extends android.content.BroadcastReceiver
    -keep public class * extends android.content.ContentProvider
    -keep public class * extends android.app.backup.BackupAgentHelper
    -keep public class * extends android.preference.Preference
    -keep public class * extends android.view.View

    #表示不混淆上述声明的两个类，这两个类我们基本也用不上，是接入Google原生的一些服务时使用的。
    -keep public class com.android.vending.licensing.ILicensingService
    -keep public class com.google.vending.licensing.ILicensingService

    #表示对android.support包下的代码不警告，因为support包中有很多代码都是在高版本中使用的，
    #如果我们的项目指定的版本比较低在打包时就会给予警告。不过support包中所有的代码都在版本兼容性上做足了判断，
    #因此不用担心代码会出问题，所以直接忽略警告就可以了。
    # 51  52 53 54 56
    -dontwarn android.support.**
    -keep class android.support.** {*;}

    #表示不混淆Activity中参数是View的方法，因为有这样一种用法，在XML中配置android:onClick=”buttonClick”属性，
    #当用户点击该按钮时就会调用Activity中的buttonClick(View view)方法，如果这个方法被混淆的话就找不到了。
    -keepclassmembers class * extends android.app.Activity{#保持类成员
        public void *(android.view.View);
        public void onEventMainThread(**);
    }
    #表示不混淆枚举中的values()和valueOf()方法。
    -keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
    }
    #表示不混淆任何一个View中的setXxx()和getXxx()方法，因为属性动画需要有相应的setter和getter的方法实现，
    #混淆了就无法工作了。
    -keep public class * extends android.view.View{# 保持自定义控件类不被混淆
        *** get*();
        void set*(***);
        public <init>(android.content.Context);
        public <init>(android.content.Context, android.util.AttributeSet);
        public <init>(android.content.Context, android.util.AttributeSet, int);
    }
    -keepclasseswithmembers class * {
        public <init>(android.content.Context, android.util.AttributeSet);
        public <init>(android.content.Context, android.util.AttributeSet, int);
    }

    #表示不混淆Parcelable实现类中的CREATOR字段，毫无疑问，CREATOR字段是绝对不能改变的，
    #包括大小写都不能变，不然整个Parcelable工作机制都会失败。
    -keep class * implements android.os.Parcelable {
      public static final android.os.Parcelable$Creator *;
    }
    -keepclassmembers class * implements java.io.Serializable {
        static final long serialVersionUID;
        private static final java.io.ObjectStreamField[] serialPersistentFields;
        private void writeObject(java.io.ObjectOutputStream);
        private void readObject(java.io.ObjectInputStream);
        java.lang.Object writeReplace();
        java.lang.Object readResolve();
    }
    #表示不混淆R文件中的所有静态字段，我们都知道R文件是通过字段来记录每个资源的id的，字段名要是被混淆了，id也就找不着了。
    -keep class **.R$* {
     *;
    }
    -keepclassmembers class * {
        void *(**On*Event);
    }
    #表示不混淆R文件中的所有静态字段，我们都知道R文件是通过字段来记录每个资源的id的，字段名要是被混淆了，id也就找不着了。
    -keepclassmembers class **.R$* {
        public static <fields>;
    }
    -keepclassmembers public class * extends android.view.View {
      void set*(***);
      *** get*();
    }
    # Remove Logging
    -assumenosideeffects class android.util.Log {
        public static *** d(...);
        public static *** w(...);
        public static *** v(...);
        public static *** i(...);
        public static *** e(...);
    }
    -keep public class org.jsoup.** {
    public *;
    }
    -keep class org.eclipse.mat.** { *; }
    -keep class com.squareup.leakcanary.** { *; }



    -keep class com.blankj.utilcode.** { *; }
    -keepclassmembers class com.blankj.utilcode.** { *; }
    -dontwarn com.blankj.utilcode.**

