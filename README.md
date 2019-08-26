# chatroom
该项目实现了用户的注册、登录、私聊。群聊。

注册功能：点击注册按钮跳转到注册界面，将要注册的信息输入，然后从文本框控件获取信息，将获取到的信息通过AccountDao类将数据保存到数据库。
登录功能：从登陆的文本框空间获取登陆的信息，然后通过查询，判断数据库是否存在该数据。成功之后，会将该用户的信息存储到服务端缓存中。
          要是失败，会在当期页面提示错误。登陆上去之后，会有提示好友上线。服务器后获取用户登录的信息，然后给已经上线的用户提示，XX
          上线了。
私聊：在每个用户界面都会显示已经上线的好友姓名，点击该姓名，会触发鼠标点击事件，判断是否是第一次创建私聊界面，如果不是，就创建私聊界面，
      发送消息时，回先将消息发送到服务器，由服务器转发给所要发送的对象。
群聊：在创建好友的时候，只有创建的人能直到有谁，其他人都不知道自己在这个群里。只有当这个人发消息后，其他人接到这个消息才知道自己在群里。
      发送的消息也是先到服务器，然后由服务器转发个每个在群里的人。
