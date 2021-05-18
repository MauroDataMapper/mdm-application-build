/*
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.application

import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.security.CatalogueUser
import uk.ac.ox.softeng.maurodatamapper.security.UserGroup
import uk.ac.ox.softeng.maurodatamapper.security.role.GroupRole
import uk.ac.ox.softeng.maurodatamapper.security.role.GroupRoleService
import uk.ac.ox.softeng.maurodatamapper.security.role.SecurableResourceGroupRole
import uk.ac.ox.softeng.maurodatamapper.security.utils.SecurityDefinition

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource

import static uk.ac.ox.softeng.maurodatamapper.util.GormUtils.checkAndSave

@Slf4j
class BootStrap implements SecurityDefinition {

    @Autowired
    MessageSource messageSource

    GroupRoleService groupRoleService

    def init = { servletContext ->

        log.debug('Main bootstrap complete')

        environments {
            production {
                CatalogueUser.withNewTransaction {

                    admins = UserGroup.findByName('administrators')
                    if (!admins) {
                        createAdminGroup('admin')
                        checkAndSave(messageSource, admins)
                    }

                    Folder folder = new Folder(
                        label: 'Example Folder',
                        createdBy: userEmailAddresses.production,
                        readableByAuthenticatedUsers: true,
                        description: 'This folder is readable by all authenticated users, and currently only editable by users in the ' +
                                     'administrators group. Future suggestions: rename this folder and alter group access.')
                    checkAndSave(messageSource, folder)

                    if (SecurableResourceGroupRole.bySecurableResourceAndGroupRoleIdAndUserGroupId(
                        folder, groupRoleService.getFromCache(GroupRole.CONTAINER_ADMIN_ROLE_NAME).groupRole.id, admins.id).count() == 0) {
                        checkAndSave(messageSource, new SecurableResourceGroupRole(
                            createdBy: userEmailAddresses.production,
                            securableResource: folder,
                            userGroup: admins,
                            groupRole: groupRoleService.getFromCache(GroupRole.CONTAINER_ADMIN_ROLE_NAME).groupRole))
                    }
                }
                log.debug('Production environment bootstrap complete')
            }
        }
    }

    def destroy = {
    }
}
