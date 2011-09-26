/*
 * Copyright 2011 David Yeung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jcoffeescript;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: takeshita
 * Date: 11/06/03
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
public interface FileChangeListener {

    public void onUpdate(String filePath);
    public void onCreate(String filePath);
    public void onDelete(String filePath);
}