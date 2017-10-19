# idea-gwt-navigator-plugin
    
You can jump between GWT services using the "Related Symbol..." action.
<br>
It allows you to
<ul>
<li>Jump from com.foo.<b>client</b>.(I)MyService             to com.foo.<b>client</b>.(I)MyService<b>Async</b> and com.foo.<b>server</b>.MyService<b>Impl</b></li>
<li>Jump from com.foo.<b>client</b>.(I)MyService<b>Async</b> to com.foo.<b>client</b>.(I)MyService and com.foo.<b>server</b>.MyService<b>Impl</b></li>
<li>Jump from com.foo.<b>server</b>.MyService<b>Impl</b>  to com.foo.<b>client</b>.(I)MyService and com.foo.<b>client</b>.(I)MyService<b>Async</b></li>
</ul>
If the caret is within a method, then you can jump from/to the related async/impl method.

<img src="https://raw.githubusercontent.com/markiewb/idea-gwt-navigator-plugin/master/doc/screenshot1.png"/>
<img src="https://raw.githubusercontent.com/markiewb/idea-gwt-navigator-plugin/master/doc/screenshot2.png"/>

Changelog:
<ul>
<li> 1.2: Jump from/to IMyService(Async) to MyServiceImpl</li>
<li> 1.1: Jump from/to corresponding async/impl method</li>
<li> 1.0: Initial version - jump to files (regardless which method is currently active)</li>
</ul>
